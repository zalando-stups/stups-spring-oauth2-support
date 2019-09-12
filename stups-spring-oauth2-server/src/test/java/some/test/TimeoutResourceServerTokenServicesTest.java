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
package some.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.StrictAssertions.assertThat;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.zalando.stups.oauth2.spring.server.DefaultAuthenticationExtractor;
import org.zalando.stups.oauth2.spring.server.TokenInfoResourceServerTokenServices;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SampleApplication.class }, webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext
@ActiveProfiles({ "custom", "tokeninfoTimeout" })
public class TimeoutResourceServerTokenServicesTest {// extends AbstractTokenInfoResourceServerTokenServicesTest {

    @Value("${local.server.port}")
    protected int port;

    @Autowired
    private TokenInfoResourceServerTokenServices tokenInfoResourceServerTokenServices;

    @Before
    public void setUp() {
        assertThat(tokenInfoResourceServerTokenServices.getAuthenticationExtractor()).isNotNull();
        assertThat(tokenInfoResourceServerTokenServices.getAuthenticationExtractor())
                .isExactlyInstanceOf(DefaultAuthenticationExtractor.class);
    }

    @Test
    public void testTimeoutBehaviour() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(new ClientHttpRequestInterceptor() {

            @Override
            public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
                    throws IOException {
                request.getHeaders().add(HttpHeaders.AUTHORIZATION, "Bearer timeout");
                request.getHeaders().add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_UTF8_VALUE);
                return execution.execute(request, body);
            }
        });
        try {
            restTemplate.getForEntity(getBasePath() + "/secured/hello/bello", String.class);
            Assertions.fail("was expecting an 401");
        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    protected String getBasePath() {
        return "http://localhost:" + port;
    }
}
