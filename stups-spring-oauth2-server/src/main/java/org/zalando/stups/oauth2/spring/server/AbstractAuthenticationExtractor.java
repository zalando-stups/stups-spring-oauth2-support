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

import org.zalando.stups.oauth2.spring.authorization.UserRolesProvider;

/**
 * Common code for current {@link AuthenticationExtractor}-implementations.
 *
 * @author  jbellmann
 */
public abstract class AbstractAuthenticationExtractor implements AuthenticationExtractor {

    private static final String UID = "uid";

    public static final String ACCESS_TOKEN = "access_token";

    public static final String REALM = "realm";

    public static final String EMPLOYEES = "/employees";

    public static final String DEFAULT_ROLE = "ROLE_USER";

    private boolean throwExceptionOnEmptyUid = true;

    @Override
    public OAuth2Authentication extractAuthentication(final Map<String, Object> tokenInfoMap, final String clientId,
            final UserRolesProvider userRolesProvider) {
        final UsernamePasswordAuthenticationToken user = createAuthenticationToken(tokenInfoMap, userRolesProvider);

        // at the moment there is other way
        final Set<String> scopes = resolveScopes(tokenInfoMap);

        return buildOAuth2Authentication(user, scopes, clientId);
    }

    protected UsernamePasswordAuthenticationToken createAuthenticationToken(final Map<String, Object> tokenInfoMap,
            final UserRolesProvider userRolesProvider) {
        List<GrantedAuthority> authorities = createAuthorityList(tokenInfoMap, userRolesProvider);

        UsernamePasswordAuthenticationToken user = new UsernamePasswordAuthenticationToken(getPrincipal(tokenInfoMap),
                "N/A", authorities);

        user.setDetails(tokenInfoMap);

        return user;
    }

    protected List<GrantedAuthority> createAuthorityList(final Map<String, Object> map,
            final UserRolesProvider userRolesProvider) {
        final String uid = (String) map.get(UID);
        final String accessToken = (String) map.get(ACCESS_TOKEN);
        final String realm = (String) map.get(REALM);
        if (EMPLOYEES.equals(realm)) {
            final List<String> userRoles = userRolesProvider.getUserRoles(uid, accessToken);
            return AuthorityUtils.createAuthorityList(userRoles.toArray(new String[userRoles.size()]));
        } else {
            return AuthorityUtils.commaSeparatedStringToAuthorityList(DEFAULT_ROLE);
        }
    }

    protected OAuth2Authentication buildOAuth2Authentication(final UsernamePasswordAuthenticationToken user,
            final Set<String> scopes, final String clientId) {

        final OAuth2Request request = new OAuth2Request(null, clientId, null, true, scopes, null, null, null, null);
        return new OAuth2Authentication(request, user);
    }

    protected abstract Set<String> resolveScopes(Map<String, Object> map);

    protected Set<String> validateUidScope(final Set<String> scopes, final Map<String, Object> map) {
        final Set<String> result = new HashSet<String>(scopes);
        final String uidValue = (String) map.get(UID);

        if (StringUtils.hasText(uidValue)) {
            result.add(UID);
        } else {
            if (isThrowExceptionOnEmptyUid()) {
                throw new InvalidTokenException("'uid' in accessToken should never be empty!");
            }
        }

        return result;
    }

    protected Object getPrincipal(final Map<String, Object> map) {
        for (final String key : getPossibleUserIdKeys()) {
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
        return new String[] {UID};

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
