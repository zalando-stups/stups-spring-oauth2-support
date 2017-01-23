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
package org.zalando.stups.oauth2.jaxws.cxf.interceptors;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;

import org.assertj.core.api.Assertions;

import org.junit.Test;

import org.mockito.Mockito;

import org.zalando.stups.tokens.AccessTokens;

public class OAuth2TokenInterceptorTest {

    @Test(expected = IllegalArgumentException.class)
    public void createWithNullAccessTokens() {
        new OAuth2TokenInterceptor(null, "aServiceId");
    }

    @Test(expected = IllegalArgumentException.class)
    public void createWithNullServiceId() {
        AccessTokens accessTokens = Mockito.mock(AccessTokens.class);
        new OAuth2TokenInterceptor(accessTokens, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createWithEmptyServiceId() {
        AccessTokens accessTokens = Mockito.mock(AccessTokens.class);
        new OAuth2TokenInterceptor(accessTokens, "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void createWithWhitespaceServiceId() {
        AccessTokens accessTokens = Mockito.mock(AccessTokens.class);
        new OAuth2TokenInterceptor(accessTokens, "   ");
    }

    @Test
    public void working() {
        String serviceId = "targetService";
        AccessTokens accessTokens = Mockito.mock(AccessTokens.class);
        Mockito.when(accessTokens.get(Mockito.eq(serviceId))).thenReturn("0123456789");

        Message message = new MessageImpl();

        OAuth2TokenInterceptor interceptor = new OAuth2TokenInterceptor(accessTokens, serviceId);
        interceptor.handleMessage(message);

        Assertions.assertThat(message.get(Message.PROTOCOL_HEADERS)).isNotNull();

        Map<String, List<String>> headers = (Map<String, List<String>>) message.get(Message.PROTOCOL_HEADERS);
        Assertions.assertThat(headers).containsValue(Collections.singletonList("Bearer 0123456789"));

        Mockito.verify(accessTokens, Mockito.atLeast(1)).get(Mockito.eq(serviceId));

    }

}
