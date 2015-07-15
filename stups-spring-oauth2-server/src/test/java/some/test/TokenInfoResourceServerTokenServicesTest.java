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
package some.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.http.AccessTokenRequiredException;
import org.springframework.security.oauth2.client.resource.BaseOAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.common.exceptions.InsufficientScopeException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestOperations;
import org.zalando.stups.oauth2.spring.client.AutoRefreshTokenProvider;
import org.zalando.stups.oauth2.spring.client.SecurityContextTokenProvider;
import org.zalando.stups.oauth2.spring.client.StupsAccessTokenProvider;
import org.zalando.stups.oauth2.spring.client.TokenProviderChain;
import org.zalando.stups.tokens.AccessTokens;
import some.test.controller.SecuredResource;
import some.test.controller.TokeninfoEndpoint;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * Full Round-Trip test.<br/>
 * Tokeninfo-Endpoint faked by {@link TokeninfoEndpoint}.<br/>
 * SecuredResource found at {@link SecuredResource}.<br/>
 *
 * @author jbellmann
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {SampleApplication.class})
@WebIntegrationTest(randomPort = false)
@ActiveProfiles("custom")
public class TokenInfoResourceServerTokenServicesTest {

    @Value("${local.server.port}")
    private int port;

    private String basePath() {
        return "http://localhost:" + port;
    }

    @Test
    public void invokeOAuthSecuredService() {

        RestOperations restOperations = buildClient("123456789");

        ResponseEntity<String> responseEntity = restOperations.getForEntity(basePath()
                + "/secured/hello/bello", String.class);

        assertThat(responseEntity.getBody(), is("hello bello"));

    }

    @Test
    public void permissionsForAllScopes() {
        RestOperations restOperations = buildClient("123456789");

        @SuppressWarnings("unchecked")
        final Set<String> scopes = restOperations.getForEntity(basePath() + "/secured/bye", Set.class).getBody();

        assertThat(scopes, equalTo(TokeninfoEndpoint.ALL_SCOPES));
    }

    @Test(expected = AccessTokenRequiredException.class)
    public void invokeOAuthSecuredServiceWithInvalidToken() {
        RestOperations restOperations = buildClient("error");

        restOperations.getForEntity(basePath() + "/secured/hello/bello", String.class);
    }

    @Test(expected = AccessTokenRequiredException.class)
    public void invokeOAuthSecuredServiceWithInvalidTokenNoUid() {
        RestOperations restOperations = buildClient("no-uid");

        restOperations.getForEntity(basePath() + "/secured/hello/bello", String.class);
    }

    @Test(expected = AccessTokenRequiredException.class)
    public void invokeOAuthSecuredServiceWithInvalidTokenEmptyUid() {
        RestOperations restOperations = buildClient("empty-uid");

        restOperations.getForEntity(basePath() + "/secured/hello/bello", String.class);
    }

    // TODO, maybe we should throw InvalidToken if 'scope' does not exist?
    @Test(expected = InsufficientScopeException.class)
    public void invokeOAuthSecuredServiceWithInvalidTokenNoScope() {
        RestOperations restOperations = buildClient("no-scope");

        restOperations.getForEntity(basePath() + "/secured/hello/bello", String.class);
    }

    protected RestOperations buildClient(final String token) {

        AccessTokens accessTokens = Mockito.mock(AccessTokens.class);
        Mockito.when(accessTokens.get(Mockito.eq("example"))).thenReturn(token);

        BaseOAuth2ProtectedResourceDetails resource = new BaseOAuth2ProtectedResourceDetails();
        resource.setClientId("what_here");

        OAuth2RestTemplate restTemplate = new OAuth2RestTemplate(resource);

        AutoRefreshTokenProvider first = new AutoRefreshTokenProvider("example", accessTokens);

        restTemplate.setAccessTokenProvider(new StupsAccessTokenProvider(
                new TokenProviderChain(new SecurityContextTokenProvider(), first)));
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());

        return restTemplate;
    }
}
