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
package org.zalando.stups.clients.kio.spring;

import static java.lang.System.currentTimeMillis;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Matchers.eq;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import static org.zalando.stups.clients.kio.spring.ResourceUtil.resource;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import org.springframework.test.web.client.MockRestServiceServer;

import org.zalando.stups.clients.kio.Application;
import org.zalando.stups.clients.kio.ApplicationBase;
import org.zalando.stups.clients.kio.Version;
import org.zalando.stups.oauth2.spring.client.StupsOAuth2RestTemplate;
import org.zalando.stups.oauth2.spring.client.StupsTokensAccessTokenProvider;
import org.zalando.stups.tokens.AccessToken;
import org.zalando.stups.tokens.AccessTokens;

/**
 * @author  jbellmann
 */
public class RestTemplateKioOperationsTest {

    private final String baseUrl = "http://localhost:8080";

    private MockRestServiceServer mockServer;

    private RestTemplateKioOperations client;

    private AccessTokens accessTokens;

    private AccessToken accessToken;

    @Before
    public void setUp() {
        accessTokens = mock(AccessTokens.class);

        final StupsOAuth2RestTemplate restTemplate = new StupsOAuth2RestTemplate(new StupsTokensAccessTokenProvider(
                    "kio", accessTokens), new HttpComponentsClientHttpRequestFactory());

        client = new RestTemplateKioOperations(restTemplate, baseUrl);

        mockServer = MockRestServiceServer.createServer(restTemplate);

        accessToken = new AccessToken("1234567890", "Bearer", 3600,
                new Date(currentTimeMillis() + TimeUnit.HOURS.toMillis(1)));
    }

    //J-
    @Test
    public void getApps() {
        when(accessTokens.getAccessToken(Mockito.any(String.class))).thenReturn(accessToken);
        mockServer.expect(requestTo(baseUrl + "/apps"))
                    .andExpect(method(GET))
                    // check header
                    .andExpect(header("Authorization", "Bearer 1234567890"))
                    .andRespond(withSuccess(resource("/getApplications"), APPLICATION_JSON));

        List<ApplicationBase> resultLists = client.listApplications();
        assertThat(resultLists).isNotNull();
        assertThat(resultLists).isNotEmpty();
        assertThat(resultLists.size()).isEqualTo(1);

        mockServer.verify();
        verify(accessTokens).getAccessToken(eq("kio"));
    }

    @Test
    public void getApplicationById() {
        when(accessTokens.getAccessToken(Mockito.any(String.class))).thenReturn(accessToken);
        mockServer.expect(requestTo(baseUrl + "/apps/kio"))
                    .andExpect(method(GET))
                    // check header
                    .andExpect(header("Authorization", "Bearer 1234567890"))
                    .andRespond(withSuccess(resource("/getApplicationById"), APPLICATION_JSON));

        Application application = client.getApplicationById("kio");
        assertThat(application).isNotNull();
        assertThat(application.getId()).isEqualTo("kio");

        mockServer.verify();
        verify(accessTokens).getAccessToken(eq("kio"));
    }

    @Test
    public void getApplicationVersion() {
        when(accessTokens.getAccessToken(Mockito.any(String.class))).thenReturn(accessToken);
        mockServer.expect(requestTo(baseUrl + "/apps/kio/versions/1"))
                    .andExpect(method(GET))
                    // check header
                    .andExpect(header("Authorization", "Bearer 1234567890"))
                    .andRespond(withSuccess(resource("/getApplicationVersion"), APPLICATION_JSON));

        Version version = client.getApplicationVersion("kio", "1");
        assertThat(version).isNotNull();
        assertThat(version.getApplicationId()).isEqualTo("kio");
        assertThat(version.getId()).isEqualTo("1");


        mockServer.verify();
        verify(accessTokens).getAccessToken(eq("kio"));
    }
    //J+

}
