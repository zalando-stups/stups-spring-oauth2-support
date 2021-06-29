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

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.BearerTokenExtractor;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.zalando.stups.oauth2.spring.authorization.DefaultUserRolesProvider;
import org.zalando.stups.oauth2.spring.authorization.UserRolesProvider;

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

    private static final String CLIENT_ID_NOT_NEEDED = "CLIENT_ID_NOT_NEEDED";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final String clientId;

    private final AuthenticationExtractor authenticationExtractor;

    private final TokenInfoRequestExecutor tokenInfoRequestExecutor;

    private final UserRolesProvider userRolesProvider;

    public TokenInfoResourceServerTokenServices(final String tokenInfoEndpointUrl) {
        this(tokenInfoEndpointUrl, CLIENT_ID_NOT_NEEDED);
    }

    public TokenInfoResourceServerTokenServices(final String tokenInfoEndpointUrl, final String clientId) {
        this(tokenInfoEndpointUrl, clientId, new DefaultAuthenticationExtractor(), new DefaultUserRolesProvider(),
            DefaultTokenInfoRequestExecutor.buildRestTemplate());
    }

    public TokenInfoResourceServerTokenServices(final String tokenInfoEndpointUrl, final String clientId,
            final UserRolesProvider userRolesProvider) {
        this(tokenInfoEndpointUrl, clientId, new DefaultAuthenticationExtractor(), userRolesProvider,
            DefaultTokenInfoRequestExecutor.buildRestTemplate());
    }

    public TokenInfoResourceServerTokenServices(final String tokenInfoEndpointUrl,
            final AuthenticationExtractor authenticationExtractor) {
        this(tokenInfoEndpointUrl, CLIENT_ID_NOT_NEEDED, authenticationExtractor, new DefaultUserRolesProvider(),
            DefaultTokenInfoRequestExecutor.buildRestTemplate());
    }

    public TokenInfoResourceServerTokenServices(final String tokenInfoEndpointUrl, final String clientId,
            final AuthenticationExtractor authenticationExtractor, final UserRolesProvider userRolesProvider,
            final RestTemplate restTemplate) {

        this(clientId, authenticationExtractor, userRolesProvider,
            new DefaultTokenInfoRequestExecutor(tokenInfoEndpointUrl, restTemplate));
    }

    public TokenInfoResourceServerTokenServices(final TokenInfoRequestExecutor tokenInfoRequestExecutor) {
        this(CLIENT_ID_NOT_NEEDED, new DefaultAuthenticationExtractor(), new DefaultUserRolesProvider(),tokenInfoRequestExecutor);
    }

    public TokenInfoResourceServerTokenServices(final String clientId,
            final AuthenticationExtractor authenticationExtractor, final UserRolesProvider userRolesProvider,
            final TokenInfoRequestExecutor tokenInfoRequestExecutor) {

        Assert.notNull(tokenInfoRequestExecutor, "'tokenInfoRequestExecutor' should never be null");
        Assert.hasText(clientId, "ClientId should never be null or empty");
        Assert.notNull(authenticationExtractor, "AuthenticationExtractor should never be null");
        Assert.notNull(userRolesProvider, "UserRolesProvider should never be null");

        this.tokenInfoRequestExecutor = tokenInfoRequestExecutor;
        this.userRolesProvider = userRolesProvider;
        this.authenticationExtractor = authenticationExtractor;
        this.clientId = clientId;
    }

    @Override
    public OAuth2Authentication loadAuthentication(final String accessToken)
            throws AuthenticationException, InvalidTokenException {

        if (!StringUtils.hasText(accessToken)) {
            throw new InvalidTokenException("'accessToken' should never be null or empty");
        }

        final Map<String, Object> map = getMap(accessToken);
        if (map == null) {
            logger.warn("'null' is not the expected response by TokenInfoEndpoint");
            throw new InvalidTokenException("Unable to fetch tokeninfo.");
        }

        if (map.containsKey("error")) {
            String description = (String) map.get("error_description");
            if (!StringUtils.hasText(description)) {
                description = (String) map.get("error");
            }
            logger.warn("tokeninfo returned error: " + description);

            throw new InvalidTokenException(description);
        }

        return this.authenticationExtractor.extractAuthentication(map, clientId, userRolesProvider);
    }

    public AuthenticationExtractor getAuthenticationExtractor() {
        return this.authenticationExtractor;
    }

    @Override
    public OAuth2AccessToken readAccessToken(final String accessToken) {
        throw new UnsupportedOperationException("Not supported: read access token");
    }

    protected Map<String, Object> getMap(final String accessToken) {
        return tokenInfoRequestExecutor.getMap(accessToken);
    }
}
