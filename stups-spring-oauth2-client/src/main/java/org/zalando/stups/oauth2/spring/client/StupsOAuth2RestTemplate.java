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

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import static org.springframework.security.oauth2.common.OAuth2AccessToken.BEARER_TYPE;

import java.io.IOException;

import java.net.URI;

import java.util.List;

import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;

import org.springframework.security.oauth2.client.token.AccessTokenProvider;

import org.springframework.web.client.RestTemplate;
import org.zalando.stups.spring.http.client.ClientHttpRequestFactorySelector;

public class StupsOAuth2RestTemplate extends RestTemplate {

    private final AccessTokenProvider accessTokenProvider;

    public StupsOAuth2RestTemplate(final AccessTokenProvider accessTokenProvider) {
        this(accessTokenProvider, ClientHttpRequestFactorySelector.getRequestFactory());
    }

    public StupsOAuth2RestTemplate(final AccessTokenProvider accessTokenProvider,
            final ClientHttpRequestFactory requestFactory) {
        super(requestFactory);
        this.accessTokenProvider = accessTokenProvider;
    }

    public StupsOAuth2RestTemplate(final AccessTokenProvider accessTokenProvider,
            final List<HttpMessageConverter<?>> messageConverters) {
        this(accessTokenProvider, ClientHttpRequestFactorySelector.getRequestFactory());
        this.setMessageConverters(messageConverters);
    }

    public StupsOAuth2RestTemplate(final AccessTokenProvider accessTokenProvider,
                                   final ClientHttpRequestFactory requestFactory, final List<HttpMessageConverter<?>> messageConverters) {
        super(requestFactory);
        this.accessTokenProvider = accessTokenProvider;
        this.setMessageConverters(messageConverters);
    }
    

    @Override
    protected ClientHttpRequest createRequest(final URI url, final HttpMethod method) throws IOException {
        final ClientHttpRequest request = super.createRequest(url, method);
        final String accessToken = accessTokenProvider.obtainAccessToken(null, null).getValue();

        request.getHeaders().set(AUTHORIZATION, BEARER_TYPE + " " + accessToken);

        return request;
    }
}
