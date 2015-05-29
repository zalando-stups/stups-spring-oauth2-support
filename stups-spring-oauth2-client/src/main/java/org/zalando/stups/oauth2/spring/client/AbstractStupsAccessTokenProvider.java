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

import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.resource.UserRedirectRequiredException;
import org.springframework.security.oauth2.client.token.AccessTokenProvider;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;

/**
 * Hide operations that are not important for us now.
 *
 * @author  jbellmann
 */
public abstract class AbstractStupsAccessTokenProvider implements AccessTokenProvider {

    @Override
    public boolean supportsResource(final OAuth2ProtectedResourceDetails resource) {
        return true;
    }

    @Override
    public OAuth2AccessToken refreshAccessToken(final OAuth2ProtectedResourceDetails resource,
            final OAuth2RefreshToken refreshToken, final AccessTokenRequest request)
        throws UserRedirectRequiredException {

        throw new UnsupportedOperationException("Not Supported 'refreshAccessToken'");
    }

    @Override
    public boolean supportsRefresh(final OAuth2ProtectedResourceDetails resource) {
        return false;
    }

}
