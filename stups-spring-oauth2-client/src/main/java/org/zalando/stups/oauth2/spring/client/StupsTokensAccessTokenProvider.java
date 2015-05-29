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
package org.zalando.stups.oauth2.spring.client;

import java.util.Optional;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.resource.UserApprovalRequiredException;
import org.springframework.security.oauth2.client.resource.UserRedirectRequiredException;
import org.springframework.security.oauth2.client.token.AccessTokenProvider;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;

import org.springframework.util.Assert;

import org.zalando.stups.tokens.AccessTokens;

/**
 * An implementation of {@link AccessTokenProvider} that delegates to {@link TokenProvider}.
 *
 * @author  jbellmann
 */
public class StupsTokensAccessTokenProvider extends AbstractStupsAccessTokenProvider {

    private TokenProvider tokenProvider;

    /**
     * {@link AccessTokenProvider} that is bound to a special client. 'serviceId' will be used to fetch the access_token
     * from {@link AccessTokens} library.
     *
     * @param  serviceId
     * @param  accessTokens
     */
    public StupsTokensAccessTokenProvider(final TokenProvider tokenProvider) {
        Assert.notNull(tokenProvider, "TokenProvider should never be null.");
        this.tokenProvider = tokenProvider;
    }

    /**
     * Delegates to {@link TokenProvider}s.
     */
    @Override
    public OAuth2AccessToken obtainAccessToken(final OAuth2ProtectedResourceDetails details,
            final AccessTokenRequest parameters) throws UserRedirectRequiredException, UserApprovalRequiredException,
        AccessDeniedException {

        // for debugging it is sometimes easier to have a temp var
        Optional<String> accessToken = tokenProvider.getToken();
        if (accessToken.isPresent()) {
            return new UpperCaseHeaderToken(accessToken.get());
        }

        throw new OAuth2Exception("No 'token' provided by tokenproviders");
    }

    /**
     * Because {@link #getTokenType()} is used in the header it should return 'Bearer' instead of 'bearer'.
     *
     * @author  jbellmann
     */
    static class UpperCaseHeaderToken extends DefaultOAuth2AccessToken {

        private static final long serialVersionUID = 1L;

        UpperCaseHeaderToken(final OAuth2AccessToken accessToken) {
            super(accessToken);
        }

        public UpperCaseHeaderToken(final String token) {
            super(token);
        }

        /**
         * because this will be used in the header it should start with 'B' instead of 'b'.
         */
        @Override
        public String getTokenType() {
            return OAuth2AccessToken.BEARER_TYPE;
        }

    }
}
