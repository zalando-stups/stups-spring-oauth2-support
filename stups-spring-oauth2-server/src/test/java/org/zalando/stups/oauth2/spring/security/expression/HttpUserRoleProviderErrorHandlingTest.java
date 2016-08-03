package org.zalando.stups.oauth2.spring.security.expression;

import static java.net.HttpURLConnection.HTTP_BAD_METHOD;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_CONFLICT;
import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_NOT_IMPLEMENTED;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static java.net.HttpURLConnection.HTTP_UNAVAILABLE;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.is;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.junit.rules.ExpectedException;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import org.zalando.stups.oauth2.spring.authorization.HttpUserRolesProvider;
import org.zalando.stups.oauth2.spring.server.TokenInfoResourceServerTokenServices;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

public class HttpUserRoleProviderErrorHandlingTest {

    @ClassRule
    public static final WireMockRule wireMockRule = new WireMockRule(10080);

    private static final ResourceSupport ResourceSupport = new ResourceSupport(RoleOAuth2ExpressionUtilsTest.class);

    private static TokenInfoResourceServerTokenServices tokenInfoServiceWithHttpUsersRoleProvider;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @BeforeClass
    public static void setUpWithHttpCode() throws IOException {

        wireMockRule.stubFor(get(urlPathEqualTo("/oauth2/tokeninfo")).willReturn(
                WireMock.aResponse().withBody(
                    ResourceSupport.resourceToString(ResourceSupport.jsonResource("tokeninfo_employee_realm")))
                        .withStatus(HTTP_OK).withHeader("Content-Type", "application/json")));

        tokenInfoServiceWithHttpUsersRoleProvider = new TokenInfoResourceServerTokenServices(
                "http://localhost:10080/oauth2/tokeninfo", "testing",
                new HttpUserRolesProvider("http://localhost:10080/api/employees/{uid}/groups"));
    }

    private static void setUpWithHttpCode(final int httpStatusCode) {
        wireMockRule.stubFor(get(urlPathEqualTo("/api/employees/ktester/groups")).willReturn(
                WireMock.aResponse().withStatus(httpStatusCode).withHeader("Content-Type", "application/json")));
    }

    @Test
    public void testAllUnhandledResponse() {
        testUnhandledStatus(HTTP_BAD_REQUEST);
        testUnhandledStatus(HTTP_UNAUTHORIZED);
        testUnhandledStatus(HTTP_FORBIDDEN);
    }

    private void testUnhandledStatus(final int httpStatusCode) {
        setUpWithHttpCode(httpStatusCode);

        OAuth2Authentication authentication = tokenInfoServiceWithHttpUsersRoleProvider.loadAuthentication("123456789");
        List<GrantedAuthority> authorities = new ArrayList<>(authentication.getAuthorities());

        assertThat(authorities.size(), is(0));
    }

    @Test
    public void testHandledClientResponses() {
        testHandledClientResponse(HTTP_NOT_FOUND);
        testHandledClientResponse(HTTP_BAD_METHOD);
        testHandledClientResponse(HTTP_CONFLICT);
    }

    private void testHandledClientResponse(final int httpStatusCode) {
        setUpWithHttpCode(httpStatusCode);

        exception.expect(HttpClientErrorException.class);
        exception.expectMessage(String.valueOf(httpStatusCode));

        tokenInfoServiceWithHttpUsersRoleProvider.loadAuthentication("123456789");
    }

    @Test
    public void testHandledServerResponses() {
        testHandledServerResponse(HTTP_INTERNAL_ERROR);
        testHandledServerResponse(HTTP_UNAVAILABLE);
        testHandledServerResponse(HTTP_NOT_IMPLEMENTED);
    }

    private void testHandledServerResponse(final int httpStatusCode) {
        setUpWithHttpCode(httpStatusCode);

        exception.expect(HttpServerErrorException.class);
        exception.expectMessage(String.valueOf(httpStatusCode));

        tokenInfoServiceWithHttpUsersRoleProvider.loadAuthentication("123456789");
    }
}
