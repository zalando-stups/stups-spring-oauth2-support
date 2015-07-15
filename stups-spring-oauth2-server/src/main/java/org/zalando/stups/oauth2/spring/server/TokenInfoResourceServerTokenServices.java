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

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.BaseOAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.BearerTokenExtractor;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;

/**
 * This component is used to create an {@link OAuth2Authentication}. Under the hood it takes the 'access_token' from the
 * client-request (done by {@link BearerTokenExtractor} ) and retrieves additional information from the installed
 * 'tokeninfo'-endpoint (https://sec.yourcompany.it/tokeninfo).<br/>
 * Afterwards it extracts 'scope' information and injects these into {@link OAuth2Authentication} object.
 *
 * @author  jbellmann
 */
public class TokenInfoResourceServerTokenServices implements ResourceServerTokenServices {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected String tokenInfoEndpointUrl;

    protected String clientId;

    private OAuth2RestOperations restTemplate;

    private final AuthenticationExtractor authenticationExtractor;

    /**
     * Specify 'tokenInfoEndpointUrl' and 'clientId' to be used by this component.
     *
     * @param  tokenInfoEndpointUrl
     * @param  clientId
     */
    public TokenInfoResourceServerTokenServices(final String tokenInfoEndpointUrl, final String clientId) {
        this(tokenInfoEndpointUrl, clientId, new DefaultAuthenticationExtractor());
    }

    public TokenInfoResourceServerTokenServices(final String tokenInfoEndpointUrl, final String clientId,
            final AuthenticationExtractor authenticationExtractor) {
        this.tokenInfoEndpointUrl = tokenInfoEndpointUrl;
        this.clientId = clientId;
        this.authenticationExtractor = authenticationExtractor;

        //
        BaseOAuth2ProtectedResourceDetails resource = new BaseOAuth2ProtectedResourceDetails();
        resource.setClientId(clientId);
        this.restTemplate = new OAuth2RestTemplate(resource);
    }

    @Override
    public OAuth2Authentication loadAuthentication(final String accessToken) throws AuthenticationException,
        InvalidTokenException {

        Map<String, Object> map = getMap(tokenInfoEndpointUrl, accessToken);

        if (map.containsKey("error")) {
            logger.debug("userinfo returned error: " + map.get("error"));
            throw new InvalidTokenException(accessToken);
        }

        return this.authenticationExtractor.extractAuthentication(map, clientId);
    }

    @Override
    public OAuth2AccessToken readAccessToken(final String accessToken) {
        throw new UnsupportedOperationException("Not supported: read access token");
    }

    protected Map<String, Object> getMap(final String tokenInfoEndpointUrl, final String accessToken) {
        logger.info("Getting token info from: " + tokenInfoEndpointUrl);

        OAuth2RestOperations restTemplate = this.restTemplate;
// restTemplate.getOAuth2ClientContext().setAccessToken(new DefaultOAuth2AccessToken(accessToken));
        restTemplate.getOAuth2ClientContext().setAccessToken(new UppercaseOAuth2AccessToken(accessToken));

        @SuppressWarnings("rawtypes")
        Map map = restTemplate.getForEntity(tokenInfoEndpointUrl, Map.class).getBody();
        @SuppressWarnings("unchecked")
        Map<String, Object> result = map;
        return result;
    }

    public AuthenticationExtractor getAuthenticationExtractor() {
        return this.authenticationExtractor;
    }

}
