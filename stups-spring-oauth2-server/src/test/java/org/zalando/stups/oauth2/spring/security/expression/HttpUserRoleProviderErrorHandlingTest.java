package org.zalando.stups.oauth2.spring.security.expression;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.zalando.stups.oauth2.spring.authorization.HttpUserRolesProvider;
import org.zalando.stups.oauth2.spring.server.TokenInfoResourceServerTokenServices;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static java.net.HttpURLConnection.HTTP_OK;

public class HttpUserRoleProviderErrorHandlingTest {

    @ClassRule
    public static final WireMockRule wireMockRule = new WireMockRule(10080);

    public static final String ROLE_PREFIX = "ROLE";

    private static final ResourceSupport ResourceSupport = new ResourceSupport(RoleOAuth2ExpressionUtilsTest.class);

    private static TokenInfoResourceServerTokenServices tokenInfoServiceWithHttpUsersRoleProvider;


    @BeforeClass
    public static void setUp() throws IOException {

        wireMockRule.stubFor(get(urlPathEqualTo("/oauth2/tokeninfo"))
                .willReturn(WireMock.aResponse()
                        .withBody(ResourceSupport.resourceToString(ResourceSupport.jsonResource("tokeninfo_employee_realm")))
                        .withStatus(HTTP_OK).withHeader("Content-Type", "application/json")));

        tokenInfoServiceWithHttpUsersRoleProvider = new TokenInfoResourceServerTokenServices("http://localhost:10080/oauth2/tokeninfo",
                "testing", new HttpUserRolesProvider("http://localhost:10080/api/employees/{uid}/groups", ROLE_PREFIX));
    }


    private static void setUp(int httpStatusCode) {
        wireMockRule.stubFor(get(urlPathEqualTo("/api/employees/ktester/groups"))
                .willReturn(WireMock.aResponse()
                        .withStatus(httpStatusCode).withHeader("Content-Type", "application/json")));
    }



}
