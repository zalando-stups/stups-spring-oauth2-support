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
package org.zalando.stups.oauth2.spring.client;

import static java.util.Collections.singletonMap;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.springframework.security.oauth2.common.OAuth2AccessToken.ACCESS_TOKEN;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;

public class SecurityContextTokenProviderTest {

    private SecurityContextTokenProvider tokenProvider;

    @Before
    public void setUp() throws Exception {
        tokenProvider = new SecurityContextTokenProvider();

        final Authentication userAuthentication = mock(Authentication.class);
        when(userAuthentication.getDetails()).thenReturn(singletonMap(ACCESS_TOKEN, "1234567890"));
        SecurityContextHolder.getContext().setAuthentication( //
                                 new OAuth2Authentication(mock(OAuth2Request.class), userAuthentication));
    }

    @After
    public void tearDown() throws Exception {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testObtainAccessToken() throws Exception {
        final OAuth2AccessToken oAuth2AccessToken = tokenProvider.obtainAccessToken(mock(
                    OAuth2ProtectedResourceDetails.class), mock(AccessTokenRequest.class));
        assertThat(oAuth2AccessToken).isNotNull();
        assertThat(oAuth2AccessToken.getValue()).isEqualTo("1234567890");
    }

    @Test(expected = OAuth2Exception.class)
    public void testTokenUnavailable() throws Exception {
        SecurityContextHolder.clearContext();
        tokenProvider.obtainAccessToken(mock(OAuth2ProtectedResourceDetails.class), mock(AccessTokenRequest.class));
    }
}
