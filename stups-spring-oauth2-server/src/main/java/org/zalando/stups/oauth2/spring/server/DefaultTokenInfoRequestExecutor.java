/**
 * Copyright (C) 2016 Zalando SE (http://tech.zalando.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zalando.stups.oauth2.spring.server;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import static org.springframework.security.oauth2.common.OAuth2AccessToken.BEARER_TYPE;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.core.ParameterizedTypeReference;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;

import org.springframework.util.Assert;

import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import org.zalando.stups.spring.http.client.ClientHttpRequestFactorySelector;

/**
 * @author  jbellmann
 */
public class DefaultTokenInfoRequestExecutor implements TokenInfoRequestExecutor {

    private final Logger logger = LoggerFactory.getLogger(DefaultTokenInfoRequestExecutor.class);

    private static final String SPACE = " ";

    private final RestOperations restOperations;

    private static final ParameterizedTypeReference<Map<String, Object>> TOKENINFO_MAP =
        new ParameterizedTypeReference<Map<String, Object>>() { };

    private final URI tokenInfoEndpointUri;

    public DefaultTokenInfoRequestExecutor(final String tokenInfoEndpointUrl) {
        this(tokenInfoEndpointUrl, buildRestTemplate());
    }

    public DefaultTokenInfoRequestExecutor(final String tokenInfoEndpointUrl, final RestOperations restOperations) {
        Assert.notNull(restOperations, "'restOperations' should never be null");
        Assert.hasText(tokenInfoEndpointUrl, "TokenInfoEndpointUrl should never be null or empty");
        try {
            new URL(tokenInfoEndpointUrl);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("TokenInfoEndpointUrl is not an URL", e);
        }

        this.restOperations = restOperations;
        this.tokenInfoEndpointUri = URI.create(tokenInfoEndpointUrl);
    }

    @Override
    public Map<String, Object> getMap(final String accessToken) {
        return doGetMap(accessToken);
    }

    protected Map<String, Object> doGetMap(final String accessToken) {
        logger.debug("Getting token-info from: {}", tokenInfoEndpointUri.toString());

        final RequestEntity<Void> entity = buildRequestEntity(tokenInfoEndpointUri, accessToken);
        try {
            return restOperations.exchange(entity, TOKENINFO_MAP).getBody();
        } catch (Exception e) {
            logger.warn("Unable to get authorisation from tokeninfo", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return errorResponse;
        }

    }

    // @formatter:off
    public static RequestEntity<Void> buildRequestEntity(final URI tokenInfoEndpointUri, final String accessToken) {
        return RequestEntity.get(tokenInfoEndpointUri).accept(MediaType.APPLICATION_JSON)
                            .header(AUTHORIZATION, BEARER_TYPE + SPACE + accessToken).build();
    }
    // @formatter:on

    /**
     * Creates a {@link RestTemplate} instance with a default {@link TokenResponseErrorHandler}.
     *
     * @see  TokenResponseErrorHandler#getDefault()
     */
    public static RestTemplate buildRestTemplate() {
        return buildRestTemplate(TokenResponseErrorHandler.getDefault());
    }

    /**
     * Creates a {@link RestTemplate} instance with a {@link TokenResponseErrorHandler} that will not do any
     * {@link ResponseErrorHandler#handleError(org.springframework.http.client.ClientHttpResponse)} for the passed
     * {@link HttpStatus}.
     *
     * @param  unhandledStatusSet
     */
    public static RestTemplate buildRestTemplate(final EnumSet<HttpStatus> unhandledStatusSet) {
        return buildRestTemplate(new TokenResponseErrorHandler(unhandledStatusSet));
    }

    /**
     * Creates a {@link RestTemplate} instance with the specified {@link ResponseErrorHandler} set.
     *
     * @param  unhandledStatusSet
     */
    public static RestTemplate buildRestTemplate(final ResponseErrorHandler responseErrorHandler) {
        Assert.notNull(responseErrorHandler, "'responseHandler' should never be null");

        final RestTemplate restTemplate = new RestTemplate(ClientHttpRequestFactorySelector.getRequestFactory());
        restTemplate.setErrorHandler(responseErrorHandler);
        return restTemplate;
    }
}
