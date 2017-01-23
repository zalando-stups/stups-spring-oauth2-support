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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.oauth2.client.resource.BaseOAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.DefaultAccessTokenRequest;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.zalando.stups.tokens.AccessToken;
import org.zalando.stups.tokens.AccessTokenUnavailableException;
import org.zalando.stups.tokens.AccessTokens;

public class StupsTokensAccessTokenProviderTest {

    private static final String TOKEN_ID = "a-token-name";

    private AccessTokens mockAccessTokens;

    private StupsTokensAccessTokenProvider accessTokenProvider;

    @Before
    public void setUp() throws Exception {
        mockAccessTokens = mock(AccessTokens.class);

        accessTokenProvider = new StupsTokensAccessTokenProvider(TOKEN_ID, mockAccessTokens);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(mockAccessTokens);
    }

    @Test
    public void testObtainAccessToken() throws Exception {
        when(mockAccessTokens.getAccessToken(anyString())).thenReturn(
                new AccessToken("12345", "bearer", 3600, tomorrow()));
        final OAuth2AccessToken accessToken = accessTokenProvider.obtainAccessToken(
                new BaseOAuth2ProtectedResourceDetails(), new DefaultAccessTokenRequest());

        assertThat(accessToken).isNotNull();
        assertThat(accessToken.getValue()).isEqualTo("12345");
        assertThat(accessToken.getTokenType()).isEqualTo("Bearer");
        assertThat(accessToken.isExpired()).isFalse();

        verify(mockAccessTokens).getAccessToken(eq(TOKEN_ID));
    }

    @Test
    public void testObtainExpiredAccessToken() throws Exception {
        when(mockAccessTokens.getAccessToken(anyString())).thenReturn(
                new AccessToken("12345", "bearer", 3600, yesterday()));
        final OAuth2AccessToken accessToken = accessTokenProvider.obtainAccessToken(
                new BaseOAuth2ProtectedResourceDetails(), new DefaultAccessTokenRequest());

        assertThat(accessToken).isNotNull();
        assertThat(accessToken.getValue()).isEqualTo("12345");
        assertThat(accessToken.getTokenType()).isEqualTo("Bearer");
        assertThat(accessToken.isExpired()).isTrue();

        verify(mockAccessTokens).getAccessToken(eq(TOKEN_ID));
    }

    @Test
    public void testObtainAccessTokenException() throws Exception {
        final AccessTokenUnavailableException unavailableAccessToken = new AccessTokenUnavailableException();
        when(mockAccessTokens.getAccessToken(anyObject())).thenThrow(unavailableAccessToken);
        try {
            accessTokenProvider.obtainAccessToken(
                    new BaseOAuth2ProtectedResourceDetails(),
                    new DefaultAccessTokenRequest());
            failBecauseExceptionWasNotThrown(OAuth2Exception.class);
        }
        catch (OAuth2Exception e) {
            assertThat(e).hasCause(unavailableAccessToken);
            verify(mockAccessTokens).getAccessToken(eq(TOKEN_ID));
        }
    }
    
    
    // copy pasted from assertj-3.1.0
    public static Date tomorrow() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        return cal.getTime();
      }
    
    public static Date yesterday() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        return cal.getTime();
      }
}