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

import static java.util.Collections.singletonMap;

import static org.assertj.core.api.StrictAssertions.assertThat;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.springframework.security.oauth2.common.OAuth2AccessToken.ACCESS_TOKEN;

import static org.zalando.stups.oauth2.spring.client.AccessTokenUtils.getAccessTokenFromSecurityContext;

import org.junit.After;
import org.junit.Test;

import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;

public class AccessTokenUtilsTest {

    @After
    public void tearDown() throws Exception {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testNonOAuth2Authentication() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("foo", "bar"));
        assertThat(getAccessTokenFromSecurityContext().get()).isNull();
    }

    @Test
    public void testMissingUserDetails() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new OAuth2Authentication(mock(OAuth2Request.class),
                mock(Authentication.class)));
        assertThat(getAccessTokenFromSecurityContext().get()).isNull();
    }

    @Test
    public void testUserDetailsIsNotAMap() throws Exception {
        final Authentication userAuthentication = mock(Authentication.class);
        when(userAuthentication.getDetails()).thenReturn("123456789");

        SecurityContextHolder.getContext().setAuthentication(new OAuth2Authentication(mock(OAuth2Request.class),
                userAuthentication));
        assertThat(getAccessTokenFromSecurityContext().get()).isNull();
    }

    @Test
    public void testGetToken() throws Exception {
        final Authentication userAuthentication = mock(Authentication.class);
        when(userAuthentication.getDetails()).thenReturn(singletonMap(ACCESS_TOKEN, "1234567890"));

        SecurityContextHolder.getContext().setAuthentication(new OAuth2Authentication(mock(OAuth2Request.class),
                userAuthentication));
        assertThat(getAccessTokenFromSecurityContext().get()).contains("1234567890");
    }
}
