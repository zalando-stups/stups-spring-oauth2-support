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
package org.zalando.stups.oauth2.spring.security.expression;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.zalando.stups.oauth2.spring.authorization.HttpUserRolesProvider;
import org.zalando.stups.oauth2.spring.server.TokenInfoResourceServerTokenServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isIn;

public class RoleOAuth2ExpressionUtilsTest {

    @ClassRule
    public static final WireMockRule wireMockRule = new WireMockRule(10080);

    public static final String ROLE_PREFIX = "ROLE";

    public static final String DEFAULT_ROLE = "ROLE_USER";

    private static TokenInfoResourceServerTokenServices tokenInfoServiceWithHttpUsersRoleProvider;

    private static TokenInfoResourceServerTokenServices tokenInfoServiceWithDefaultUsersRoleProvider;

    private static ResourceSupport ResourceSupport = new ResourceSupport(RoleOAuth2ExpressionUtilsTest.class);

    @BeforeClass
    public static void setUp() throws IOException {

        wireMockRule.stubFor(get(urlPathEqualTo("/oauth2/tokeninfo"))
                .willReturn(WireMock.aResponse()
                .withBody(ResourceSupport.resourceToString(ResourceSupport.jsonResource("tokeninfo_employee_realm")))
                .withStatus(HTTP_OK).withHeader("Content-Type", "application/json")));

        wireMockRule.stubFor(get(urlPathEqualTo("/api/employees/ktester/groups"))
                .willReturn(WireMock.aResponse()
                .withBody(ResourceSupport.resourceToString(ResourceSupport.jsonResource("user_roles")))
                .withStatus(HTTP_OK).withHeader("Content-Type", "application/json")));

        tokenInfoServiceWithHttpUsersRoleProvider = new TokenInfoResourceServerTokenServices("http://localhost:10080/oauth2/tokeninfo",
                "testing", new HttpUserRolesProvider("http://localhost:10080/api/employees/{uid}/groups", ROLE_PREFIX));

        tokenInfoServiceWithDefaultUsersRoleProvider = new TokenInfoResourceServerTokenServices("http://localhost:10080/oauth2/tokeninfo",
                "testing");
    }

    @Test
    public void testWithHttpUserRolesProvider() {
        List<String> expectedRolesWithPrefix = Arrays.asList(ROLE_PREFIX + "_" + "unit1/APP1/TestRole1",
                                                             ROLE_PREFIX + "_" + "unit2/APP2/TestRole2",
                                                             ROLE_PREFIX + "_" + "unit3/APP3/TestRole3");

        OAuth2Authentication authentication = tokenInfoServiceWithHttpUsersRoleProvider.loadAuthentication("123456789");
        List<GrantedAuthority> authorities = new ArrayList<>(authentication.getAuthorities());

        assertThat(authorities.size(), is(3));

        for(GrantedAuthority grantedAuthority : authentication.getAuthorities()) {
            assertThat(grantedAuthority.getAuthority(), isIn(expectedRolesWithPrefix));
        }
    }

    @Test
    public void testWithDefaultUserRolesProvider() {
        OAuth2Authentication authentication = tokenInfoServiceWithDefaultUsersRoleProvider.loadAuthentication("123456789");
        List<GrantedAuthority> authorities = new ArrayList<>(authentication.getAuthorities());

        assertThat(authorities.size(), is(1));
        assertThat(authorities.get(0).getAuthority(), is(DEFAULT_ROLE));
    }

}
