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

import static java.lang.System.currentTimeMillis;
import static org.springframework.security.oauth2.common.OAuth2AccessToken.ACCESS_TOKEN;
import static org.springframework.security.oauth2.common.OAuth2AccessToken.EXPIRES_IN;
import static org.springframework.security.oauth2.common.OAuth2AccessToken.TOKEN_TYPE;
import static org.springframework.util.StringUtils.capitalize;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.util.Assert;
import org.zalando.stups.tokens.AccessToken;
import org.zalando.stups.tokens.AccessTokenUnavailableException;
import org.zalando.stups.tokens.AccessTokens;

/**
 * An AccessTokenProvider that utilizes the {@link AccessTokens} object,
 * defined in <a href="https://github.com/zalando-stups/tokens">Zalando STUPS' "tokens" library</a>,
 * to obtain an access token.
 */
public class StupsTokensAccessTokenProvider extends AbstractStupsAccessTokenProvider {

    private final String tokenId;

    private final AccessTokens tokens;

    public StupsTokensAccessTokenProvider(String tokenId, AccessTokens tokens) {
        Assert.hasText(tokenId, "tokenId cannot be left blank");
        Assert.notNull(tokens, "tokens must not be null");
        this.tokenId = tokenId;
        this.tokens = tokens;
    }

    @Override
    public OAuth2AccessToken obtainAccessToken(final OAuth2ProtectedResourceDetails details,
            final AccessTokenRequest parameters) {
        final AccessToken accessToken;
        try {
            accessToken = tokens.getAccessToken(tokenId);
        }
        catch (final AccessTokenUnavailableException e) {
            throw new OAuth2Exception("Could not obtain access token.", e);
        }

        final Map<String, String> tokenParams = new HashMap<>();
        tokenParams.put(ACCESS_TOKEN, accessToken.getToken());
        tokenParams.put(TOKEN_TYPE, OAuth2AccessToken.BEARER_TYPE);
        tokenParams.put(EXPIRES_IN, secondsTo(accessToken.getValidUntil()));
        return DefaultOAuth2AccessToken.valueOf(tokenParams);
    }

    private String secondsTo(Date until) {
        return String.valueOf((until.getTime() - currentTimeMillis()) / 1000);
    }
}
