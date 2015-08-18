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

import static org.assertj.core.api.StrictAssertions.assertThat;

import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.resource.UserApprovalRequiredException;
import org.springframework.security.oauth2.client.resource.UserRedirectRequiredException;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;

public class AbstractStupsAccessTokenProviderTest {

    private AbstractStupsAccessTokenProvider accessTokenProvider;

    @Before
    public void setUp() throws Exception {
        accessTokenProvider = new AbstractStupsAccessTokenProvider() {
            @Override
            public OAuth2AccessToken obtainAccessToken(final OAuth2ProtectedResourceDetails details,
                    final AccessTokenRequest parameters) throws UserRedirectRequiredException,
                UserApprovalRequiredException, AccessDeniedException {
                return null;
            }
        };
    }

    @Test
    public void testSupportsResource() throws Exception {
        assertThat(accessTokenProvider.supportsResource(mock(OAuth2ProtectedResourceDetails.class))).isTrue();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRefreshAccessToken() throws Exception {
        accessTokenProvider.refreshAccessToken(mock(OAuth2ProtectedResourceDetails.class),
            mock(OAuth2RefreshToken.class), mock(AccessTokenRequest.class));
    }

    @Test
    public void testSupportsRefresh() throws Exception {
        assertThat(accessTokenProvider.supportsRefresh(mock(OAuth2ProtectedResourceDetails.class))).isFalse();
    }
}
