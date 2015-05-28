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

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import static org.zalando.stups.clients.kio.spring.ResourceUtil.resource;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.BaseOAuth2ProtectedResourceDetails;

import org.springframework.test.web.client.MockRestServiceServer;

import org.zalando.stups.clients.kio.Application;
import org.zalando.stups.clients.kio.ApplicationBase;
import org.zalando.stups.clients.kio.Version;
import org.zalando.stups.oauth2.spring.client.StupsTokensAccessTokenProvider;
import org.zalando.stups.tokens.AccessTokens;

/**
 * @author  jbellmann
 */
public class RestTemplateKioOperationsTest {

    private final String baseUrl = "http://localhost:8080";

    private OAuth2RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    private RestTemplateKioOperations client;

    private AccessTokens accessTokens;

    @Before
    public void setUp() {

        accessTokens = Mockito.mock(AccessTokens.class);

        BaseOAuth2ProtectedResourceDetails resource = new BaseOAuth2ProtectedResourceDetails();
        resource.setClientId("what_here");

        restTemplate = new OAuth2RestTemplate(resource);

        // here is the token-provider
        restTemplate.setAccessTokenProvider(new StupsTokensAccessTokenProvider("kio", accessTokens));
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());

        client = new RestTemplateKioOperations(restTemplate, baseUrl);

        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    //J-
    @Test
    public void getApps() {
        when(accessTokens.get(Mockito.any(String.class))).thenReturn("1234567890");
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
        verify(accessTokens, atLeast(1)).get(Mockito.any(String.class));
    }

    @Test
    public void getApplicationById() {
        when(accessTokens.get(Mockito.any(String.class))).thenReturn("1234567890");
        mockServer.expect(requestTo(baseUrl + "/apps/kio"))
                    .andExpect(method(GET))
                    // check header
                    .andExpect(header("Authorization", "Bearer 1234567890"))
                    .andRespond(withSuccess(resource("/getApplicationById"), APPLICATION_JSON));

        Application application = client.getApplicationById("kio");
        assertThat(application).isNotNull();
        assertThat(application.getId()).isEqualTo("kio");

        mockServer.verify();
        verify(accessTokens, atLeast(1)).get(Mockito.any(String.class));
    }

    @Test
    public void getApplicationVersion() {
        when(accessTokens.get(Mockito.any(String.class))).thenReturn("1234567890");
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
        verify(accessTokens, atLeast(1)).get(Mockito.any(String.class));
    }
    //J+

}
