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

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.AccessTokenProvider;

import org.zalando.stups.tokens.AccessTokens;

/**
 * @author  jbellmann
 */
public class StupsTokensAccessTokenProviderTest {

    private static final String TEST_SERVICE_ID = "testServiceId";
    private static final String DEFAULT_TOKEN = "123456789";

    private AccessTokens accessTokens;

    private AccessTokenProvider accessTokenProvider;

    //
    private OAuth2ProtectedResourceDetails details;

    @Before
    public void setUp() {
        accessTokens = Mockito.mock(AccessTokens.class);
        accessTokenProvider = new StupsTokensAccessTokenProvider(TEST_SERVICE_ID, accessTokens);

        details = Mockito.mock(OAuth2ProtectedResourceDetails.class);

        when(accessTokens.get(Mockito.eq(TEST_SERVICE_ID))).thenReturn(DEFAULT_TOKEN);

    }

    @Test(expected = UnsupportedOperationException.class)
    public void refreshingShouldThrowException() {
        accessTokenProvider.refreshAccessToken(details, null, null);
    }

    @Test
    public void refreshIsNotSupported() {
        assertThat(accessTokenProvider.supportsRefresh(details)).isFalse();
    }

    @Test
    public void supportsResourceShouldBeTrue() {

        assertThat(accessTokenProvider.supportsResource(details)).isTrue();
    }

}
