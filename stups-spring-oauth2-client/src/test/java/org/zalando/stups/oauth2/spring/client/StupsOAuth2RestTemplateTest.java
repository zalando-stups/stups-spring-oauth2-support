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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.oauth2.common.OAuth2AccessToken.ACCESS_TOKEN;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.List;

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.AccessTokenProvider;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.test.web.client.MockRestServiceServer;
import org.zalando.stups.spring.http.client.ClientHttpRequestFactorySelector;

public class StupsOAuth2RestTemplateTest {

    private AccessTokenProvider mockAccessTokenProvider;

    private MockRestServiceServer mockServer;

    private StupsOAuth2RestTemplate restTemplate;

    @Before
    public void setUp() {
        mockAccessTokenProvider = mock(AccessTokenProvider.class);
        restTemplate = new StupsOAuth2RestTemplate(mockAccessTokenProvider);
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    public void testRequestWithToken() {
        when(mockAccessTokenProvider //
                .obtainAccessToken(isNull(OAuth2ProtectedResourceDetails.class), isNull(AccessTokenRequest.class))) //
                        .thenReturn(DefaultOAuth2AccessToken.valueOf(singletonMap(ACCESS_TOKEN, "1234567890")));

        mockServer.expect(requestTo("/")) //
                .andExpect(header("Authorization", "Bearer 1234567890")) //
                .andRespond(withSuccess());

        restTemplate.getForObject("/", String.class);

        mockServer.verify();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRestTemplateInitialisationWithIllegalArguments1() {
        new StupsOAuth2RestTemplate(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRestTemplateInitialisationWithIllegalArguments2() {
        List<HttpMessageConverter<?>> messageConverterList = null;
        new StupsOAuth2RestTemplate(null, messageConverterList);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRestTemplateInitialisationWithIllegalArguments3() {
        List<HttpMessageConverter<?>> messageConverterList = Lists.emptyList();
        AccessTokenProvider accessTokenProvider = mock(AccessTokenProvider.class);
        new StupsOAuth2RestTemplate(accessTokenProvider, messageConverterList);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRestTemplateInitialisationWithIllegalArguments4() {
        ClientHttpRequestFactory requestFactory = null;
        new StupsOAuth2RestTemplate(null, requestFactory);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRestTemplateInitialisationWithIllegalArguments5() {
        new StupsOAuth2RestTemplate(null, null, null);
    }

    @Test
    public void initializeWithClientHttpRequestFactory() {
        List<HttpMessageConverter<?>> converters = Lists.newArrayList();
        converters.add(new MappingJackson2HttpMessageConverter());
        new StupsOAuth2RestTemplate(mockAccessTokenProvider, ClientHttpRequestFactorySelector.getRequestFactory(),
                converters);
    }

    @Test
    public void initializeWithoutClientHttpRequestFactory() {
        List<HttpMessageConverter<?>> converters = Lists.newArrayList();
        converters.add(new MappingJackson2HttpMessageConverter());
        new StupsOAuth2RestTemplate(mockAccessTokenProvider, converters);
    }
}
