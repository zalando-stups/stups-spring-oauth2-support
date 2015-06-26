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
package org.zalando.stups.oauth2.httpcomponents;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.protocol.HttpContext;

import org.assertj.core.api.Assertions;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.zalando.stups.tokens.AccessTokens;

/**
 * @author  jbellmann
 */
public class AccessTokensRequestInterceptorTest {

    private HttpContext httpContext;
    private AccessTokens accessTokens;

    @Before
    public void setUp() {
        httpContext = Mockito.mock(HttpContext.class);
        accessTokens = Mockito.mock(AccessTokens.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void initializeWithNullTokenIdShouldFail() {
        new AccessTokensRequestInterceptor(null, accessTokens);
    }

    @Test(expected = IllegalArgumentException.class)
    public void initializeWithEmptyTokenIdShouldFail() {
        new AccessTokensRequestInterceptor("", accessTokens);
    }

    @Test(expected = IllegalArgumentException.class)
    public void initializeWithWhitespaceTokenIdShouldFail() {
        new AccessTokensRequestInterceptor("  ", accessTokens);
    }

    @Test(expected = IllegalArgumentException.class)
    public void initializeWithNullAccessTokensShouldFail() {
        new AccessTokensRequestInterceptor("kio", null);
    }

    @Test
    public void testRequestInterceptor() throws HttpException, IOException {
        AccessTokens accessTokens = Mockito.mock(AccessTokens.class);
        Mockito.when(accessTokens.get(Mockito.any())).thenReturn("TEST_TOKEN");

        AccessTokensRequestInterceptor interceptor = new AccessTokensRequestInterceptor("kio", accessTokens);
        BasicHttpRequest request = new BasicHttpRequest("GET", "http://anyendpoint.test");

        interceptor.process(request, httpContext);

        Assertions.assertThat(request.getHeaders("access_token")).isNotEmpty();

        Assertions.assertThat(request.getHeaders("access_token")[0].getValue()).isEqualTo("TEST_TOKEN");

        Mockito.verify(accessTokens, Mockito.atMost(1)).get(Mockito.any());

    }

    @Test(expected = HttpException.class)
    public void testRequestInterceptorThrowsHttpException() throws HttpException, IOException {
        AccessTokens accessTokens = Mockito.mock(AccessTokens.class);
        Mockito.when(accessTokens.get(Mockito.any())).thenThrow(new RuntimeException("THROWN_BY_MOCK"));

        AccessTokensRequestInterceptor interceptor = new AccessTokensRequestInterceptor("kio", accessTokens);
        BasicHttpRequest request = new BasicHttpRequest("GET", "http://anyendpoint.test");

        interceptor.process(request, httpContext);

    }
}
