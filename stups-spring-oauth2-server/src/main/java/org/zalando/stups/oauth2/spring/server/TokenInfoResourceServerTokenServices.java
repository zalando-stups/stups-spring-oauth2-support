/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.http.OAuth2ErrorHandler;
import org.springframework.security.oauth2.client.resource.BaseOAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.BearerTokenExtractor;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.zalando.stups.spring.http.client.ClientHttpRequestFactorySelector;

/**
 * This component is used to create an {@link OAuth2Authentication}. Under the
 * hood it takes the 'access_token' from the client-request (done by
 * {@link BearerTokenExtractor} ) and retrieves additional information from the
 * installed 'tokeninfo'-endpoint (https://sec.yourcompany.it/tokeninfo).<br/>
 * Afterwards it extracts 'scope' information and injects these into
 * {@link OAuth2Authentication} object.
 *
 * @author jbellmann
 */
public class TokenInfoResourceServerTokenServices implements ResourceServerTokenServices {

    private static final String SPACE = " ";

    private static final String CLIENT_ID_NOT_NEEDED = "CLIENT_ID_NOT_NEEDED";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final String clientId;

    private final RestTemplate restTemplate;

    private final AuthenticationExtractor authenticationExtractor;

    private final URI tokenInfoEndpointUri;

    /**
     * Specify 'tokenInfoEndpointUrl' to be used by this component.
     *
     * @param tokenInfoEndpointUrl
     */
    public TokenInfoResourceServerTokenServices(final String tokenInfoEndpointUrl) {
        this(tokenInfoEndpointUrl, CLIENT_ID_NOT_NEEDED);
    }

    public TokenInfoResourceServerTokenServices(final String tokenInfoEndpointUrl, final String clientId) {
        this(tokenInfoEndpointUrl, clientId, new DefaultAuthenticationExtractor(), buildRestTemplate());
    }

    public TokenInfoResourceServerTokenServices(final String tokenInfoEndpointUrl,
            final AuthenticationExtractor authenticationExtractor) {
        this(tokenInfoEndpointUrl, CLIENT_ID_NOT_NEEDED, authenticationExtractor, buildRestTemplate());
    }

    public TokenInfoResourceServerTokenServices(final String tokenInfoEndpointUrl, final String clientId,
            final AuthenticationExtractor authenticationExtractor, final RestTemplate restTemplate) {

        Assert.hasText(tokenInfoEndpointUrl, "TokenInfoEndpointUrl should never be null or empty");
        try {
            new URL(tokenInfoEndpointUrl);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("TokenInfoEndpointUrl is not an URL", e);
        }

        Assert.hasText(clientId, "ClientId should never be null or empty");
        Assert.notNull(authenticationExtractor, "AuthenticationExtractor should never be null");
        Assert.notNull(restTemplate, "RestTemplate should not be null");

        this.tokenInfoEndpointUri = URI.create(tokenInfoEndpointUrl);
        this.authenticationExtractor = authenticationExtractor;
        this.restTemplate = restTemplate;

        this.clientId = clientId;
    }

    @Override
    public OAuth2Authentication loadAuthentication(final String accessToken)
            throws AuthenticationException, InvalidTokenException {

        if (!StringUtils.hasText(accessToken)) {
            throw new InvalidTokenException("'accessToken' should never be null or empty");
        }

        Map<String, Object> map = null;

        try {
            map = getMap(accessToken);
        } catch (OAuth2Exception e) {
            throw e;
        } catch (Exception e) {
            throw new TokenInfoEndpointException("Retrieving information to 'accessToken' failed.", e);
        }

        if (map.containsKey("error")) {
            logger.debug("userinfo returned error: " + map.get("error"));

            String description = (String) map.get("error_description");
            if (!StringUtils.hasText(description)) {
                description = (String) map.get("error");
            }
            throw new InvalidTokenException(description);
        }

        return this.authenticationExtractor.extractAuthentication(map, clientId);
    }

    //@formatter:off
    public static RequestEntity<Void> buildRequestEntity(URI tokenInfoEndpointUri, String accessToken) {
        return RequestEntity.get(tokenInfoEndpointUri)
                            .accept(MediaType.APPLICATION_JSON)
                            .header(HttpHeaders.AUTHORIZATION, OAuth2AccessToken.BEARER_TYPE + SPACE + accessToken)
                            .build();
    }
    //@formatter:on

    public AuthenticationExtractor getAuthenticationExtractor() {
        return this.authenticationExtractor;
    }

    public static RestTemplate buildRestTemplate() {
        RestTemplate restTemplate = new RestTemplate(ClientHttpRequestFactorySelector.getRequestFactory());
        final BaseOAuth2ProtectedResourceDetails resource = new BaseOAuth2ProtectedResourceDetails();
        resource.setClientId("unused");
        restTemplate.setErrorHandler(new OAuth2ErrorHandler(resource));
        return restTemplate;
    }

    static class TokenInfoEndpointException extends OAuth2Exception {

        private static final long serialVersionUID = 1L;

        private static final String DEFAULT_MESSAGE = "Unknown Exception when calling TokenInfoEndpoint";

        public TokenInfoEndpointException(Throwable t) {
            this(DEFAULT_MESSAGE, t);
        }

        public TokenInfoEndpointException(String msg, Throwable t) {
            super(msg, t);
        }
    }

    @Override
    public OAuth2AccessToken readAccessToken(final String accessToken) {
        throw new UnsupportedOperationException("Not supported: read access token");
    }

    protected Map<String, Object> getMap(final String accessToken) {
        logger.debug("Getting token-info from: {}", tokenInfoEndpointUri.toString());

        RequestEntity<Void> entity = buildRequestEntity(tokenInfoEndpointUri, accessToken);
        @SuppressWarnings("rawtypes")
        Map map = restTemplate.exchange(entity, Map.class).getBody();

        @SuppressWarnings("unchecked")
        Map<String, Object> result = map;
        return result;
    }

}
