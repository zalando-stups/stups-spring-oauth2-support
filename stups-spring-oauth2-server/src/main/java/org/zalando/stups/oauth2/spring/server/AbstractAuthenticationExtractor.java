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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.util.StringUtils;

/**
 * Common code for current {@link AuthenticationExtractor}-implementations.
 *
 * @author jbellmann
 */
public abstract class AbstractAuthenticationExtractor implements AuthenticationExtractor {

    private static final String UID_SCOPE = "uid";

    private boolean throwExceptionOnEmptyUid = true;

    @Override
    public OAuth2Authentication extractAuthentication(final Map<String, Object> tokenInfoMap, final String clientId) {
        UsernamePasswordAuthenticationToken user = createAuthenticationToken(tokenInfoMap);

        // at the moment there is other way
        Set<String> scopes = resolveScopes(tokenInfoMap);

        return buildOAuth2Authentication(user, scopes, clientId);
    }

    protected UsernamePasswordAuthenticationToken createAuthenticationToken(final Map<String, Object> tokenInfoMap) {
        List<GrantedAuthority> authorities = createAuthorityList(tokenInfoMap);

        UsernamePasswordAuthenticationToken user = new UsernamePasswordAuthenticationToken(getPrincipal(tokenInfoMap), "N/A",
                authorities);

        user.setDetails(tokenInfoMap);

        return user;
    }

    protected List<GrantedAuthority> createAuthorityList(final Map<String, Object> map) {
        return AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER");
    }

    protected OAuth2Authentication buildOAuth2Authentication(UsernamePasswordAuthenticationToken user,
            Set<String> scopes, String clientId) {

        OAuth2Request request = new OAuth2Request(null, clientId, null, true, scopes, null, null, null, null);
        return new OAuth2Authentication(request, user);
    }

    protected abstract Set<String> resolveScopes(Map<String, Object> map);

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
     * There is not standardized name for the 'userId' in the
     * 'TokenInfo'-Object.
     *
     * @return
     */
    protected String[] getPossibleUserIdKeys() {

        // we only use 'uid' at the moment for userids
        return new String[] { UID_SCOPE };

        // return new String[] {"uid", "user", "username", "userid", "user_id",
        // "login", "id", "name"};
    }

    public boolean isThrowExceptionOnEmptyUid() {
        return throwExceptionOnEmptyUid;
    }

    public void setThrowExceptionOnEmptyUid(final boolean throwExceptionOnEmptyUid) {
        this.throwExceptionOnEmptyUid = throwExceptionOnEmptyUid;
    }
}
