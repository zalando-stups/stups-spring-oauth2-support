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

import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import org.assertj.core.api.Assertions;

import org.junit.Test;

import org.junit.runner.RunWith;

import org.mockito.Mockito;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;

import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.zalando.stups.tokens.AccessTokens;

/**
 * @author  jbellmann
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {ExampleApplication.class})
@WebIntegrationTest(randomPort = true)
public class RoundtripTest {

    private static final String SERVICE_ID = "kio";

    private static final String KIO_TEST_TOKEN = "KIO_TEST_TOKEN";

    private final Logger LOG = LoggerFactory.getLogger(RoundtripTest.class);

    @Value("${local.server.port}")
    int port;

    @Test
    public void simpleRoundTrip() {
        LOG.info("start roundtrip ...");

        AccessTokens accessTokens = Mockito.mock(AccessTokens.class);
        Mockito.when(accessTokens.get(Mockito.eq(SERVICE_ID))).thenReturn(KIO_TEST_TOKEN);

        HttpRequestInterceptor interceptor = new AccessTokensRequestInterceptor(SERVICE_ID, accessTokens);
        HttpClient client = HttpClientBuilder.create().addInterceptorFirst(interceptor).build();

        HttpGet getRequest = new HttpGet("http://localhost:" + port + "/test");
        try {
            HttpResponse response = client.execute(getRequest);
            String responseString = EntityUtils.toString(response.getEntity());
            Assertions.assertThat(responseString).isEqualTo(KIO_TEST_TOKEN);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        LOG.info("roundtrip ended.");
    }
}
