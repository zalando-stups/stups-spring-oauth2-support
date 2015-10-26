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

import static org.springframework.security.oauth2.common.OAuth2AccessToken.ACCESS_TOKEN;
import static org.springframework.security.oauth2.common.OAuth2AccessToken.BEARER_TYPE;
import static org.springframework.security.oauth2.common.OAuth2AccessToken.TOKEN_TYPE;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;

import com.google.common.base.Optional;

/**
 * Looks in the {@link SecurityContext} for an access_token.
 *
 * @author  jbellmann
 */
public class SecurityContextTokenProvider extends AbstractStupsAccessTokenProvider {

    @Override
    public OAuth2AccessToken obtainAccessToken(final OAuth2ProtectedResourceDetails details,
            final AccessTokenRequest parameters) {
        final Optional<String> accessToken = AccessTokenUtils.getAccessTokenFromSecurityContext();
        if(!accessToken.isPresent()){
        	throw new OAuth2Exception("No access token available in current security context");
        }
        final Map<String, String> tokenParams = new HashMap<>();
        tokenParams.put(ACCESS_TOKEN, accessToken.get());
        tokenParams.put(TOKEN_TYPE, BEARER_TYPE);
        return DefaultOAuth2AccessToken.valueOf(tokenParams);
    }

}
