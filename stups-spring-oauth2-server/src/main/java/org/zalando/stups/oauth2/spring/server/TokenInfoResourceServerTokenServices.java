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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.BaseOAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.authentication.BearerTokenExtractor;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.util.StringUtils;

/**
 * This component is used to create an {@link OAuth2Authentication}. Under the hood it takes the 'access_token' from the
 * client-request (done by {@link BearerTokenExtractor} ) and retrieves additional information from the installed
 * 'tokeninfo'-endpoint (https://sec.yourcompany.it/tokeninfo).<br/>
 * Afterwards it extracts 'scope' information and injects these into {@link OAuth2Authentication} object.
 *
 * @author  jbellmann
 */
public class TokenInfoResourceServerTokenServices implements ResourceServerTokenServices {

    private static final String UID_SCOPE = "uid";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected String tokenInfoEndpointUrl;

    protected String clientId;

    private OAuth2RestOperations restTemplate;

    private boolean throwExceptionOnEmptyUid = true;

    /**
     * Specify 'tokenInfoEndpointUrl' and 'clientId' to be used by this component.
     *
     * @param  tokenInfoEndpointUrl
     * @param  clientId
     */
    public TokenInfoResourceServerTokenServices(final String tokenInfoEndpointUrl, final String clientId) {
        this.tokenInfoEndpointUrl = tokenInfoEndpointUrl;
        this.clientId = clientId;

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

        return extractAuthentication(map);
    }

    protected OAuth2Authentication extractAuthentication(final Map<String, Object> map) {
        UsernamePasswordAuthenticationToken user = new UsernamePasswordAuthenticationToken(getPrincipal(map), "N/A",
                AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER"));
        user.setDetails(map);

        // at the moment there is other way
        Set<String> scopes = getScopesWithPermissionTrueFromMap(map);

        scopes = validateUidScope(scopes, map);

        //
        OAuth2Request request = new OAuth2Request(null, clientId, null, true, scopes, null, null, null, null);
        return new OAuth2Authentication(request, user);
    }

    protected Set<String> validateUidScope(final Set<String> scopes, final Map<String, Object> map) {
        Set<String> result = new HashSet<String>(scopes);
        String uidValue = (String) map.get(UID_SCOPE);

        if (StringUtils.hasText(uidValue)) {
            result.add(UID_SCOPE);
        } else {
            if (isThrowExceptionOnEmptyUid()) {
                throw new InvalidTokenException("'uid' in accessToken should never be empty!");
            }
        }

        return result;
    }

    public boolean isThrowExceptionOnEmptyUid() {
        return throwExceptionOnEmptyUid;
    }

    public void setThrowExceptionOnEmptyUid(final boolean throwExceptionOnEmptyUid) {
        this.throwExceptionOnEmptyUid = throwExceptionOnEmptyUid;
    }

    /**
     * Only put scopes with value 'true' into result.
     *
     * @param   map
     *
     * @return  validated scopes
     */
    protected Set<String> getScopesWithPermissionTrueFromMap(final Map<String, Object> map) {
        Set<String> scopes = new HashSet<String>();
        Set<String> permissions = new HashSet<String>();
        try {
            Object scopeValue = map.get("scope");
            if (scopeValue != null) {
                if (scopeValue instanceof ArrayList) {
                    ArrayList<String> scopeValueList = (ArrayList<String>) scopeValue;
                    scopes.addAll(scopeValueList);
                } else {
                    logger.warn("scope-value is {}", scopeValue.getClass().getName());
                }
            }

            // important part, check the scope has the permission, indicated by 'true' as value
            for (String scope : scopes) {
                Object permission = map.get(scope);
                if (permission != null) {
                    if (Boolean.parseBoolean(permission.toString())) {
                        permissions.add(scope);
                    }
                }
            }

        } catch (Exception e) {
            logger.error("Unable to get 'scope' value from map", e);
        }

        return permissions;
    }

    protected Object getPrincipal(final Map<String, Object> map) {
        for (String key : getPossibleUserIdKeys()) {
            if (map.containsKey(key)) {
                return map.get(key);
            }
        }

        throw new InvalidTokenException("No 'uid'-scope found in access-token!");

        // return "unknown";
    }

    /**
     * There is not standardized name for the 'userId' in the 'TokenInfo'-Object.
     *
     * @return
     */
    protected String[] getPossibleUserIdKeys() {

        // we only use 'uid' at the moment for userids
        return new String[] {UID_SCOPE};

// return new String[] {"uid", "user", "username", "userid", "user_id", "login", "id", "name"};
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

}
