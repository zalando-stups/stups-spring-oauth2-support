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

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static java.net.HttpURLConnection.HTTP_OK;

import java.io.IOException;
import java.nio.charset.Charset;

import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.util.StreamUtils;
import org.zalando.stups.oauth2.spring.server.TokenInfoResourceServerTokenServices;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

/**
 * 
 * @author jbellmann
 *
 */
public class RealmOAuth2ExpressionUtilsTest {

    @ClassRule
    public static final WireMockRule wireMockRule = new WireMockRule(10080);

    private static TokenInfoResourceServerTokenServices tokenInfoService;

    private static ResourceSupport ResourceSupport = new ResourceSupport(RealmOAuth2ExpressionUtilsTest.class);

    @BeforeClass
    public static void setUp() throws IOException {
        wireMockRule
                .stubFor(
                        get(urlPathEqualTo("/oauth2/tokeninfo"))
                                .willReturn(WireMock.aResponse()
                                        .withBody(ResourceSupport
                                                .resourceToString(ResourceSupport.jsonResource("tokeninfo")))
                                .withStatus(HTTP_OK).withHeader("Content-Type", "application/json")));
        tokenInfoService = new TokenInfoResourceServerTokenServices("http://localhost:10080/oauth2/tokeninfo",
                "testing");

    }

    @Test
    public void testHasKaiserRealm() {
        OAuth2Authentication authentication = tokenInfoService.loadAuthentication("123456789");
        boolean result = RealmOAuth2ExpressionUtils.hasAnyRealm(authentication, new String[] { "kaiser" });
        Assertions.assertThat(result).isFalse();
    }

    @Test
    public void testHasCustomRealm() {
        OAuth2Authentication authentication = tokenInfoService.loadAuthentication("123456789");
        boolean result = RealmOAuth2ExpressionUtils.hasAnyRealm(authentication, new String[] { "customrealm" });
        Assertions.assertThat(result).isFalse();
    }

    @Test
    public void testHasCustomRealmWithSlash() {
        OAuth2Authentication authentication = tokenInfoService.loadAuthentication("123456789");
        boolean result = RealmOAuth2ExpressionUtils.hasAnyRealm(authentication, new String[] { "/customrealm" });
        Assertions.assertThat(result).isTrue();
    }

    @Test
    public void testHasCustomRealmWithMulti() {
        OAuth2Authentication authentication = tokenInfoService.loadAuthentication("123456789");
        boolean result = RealmOAuth2ExpressionUtils.hasAnyRealm(authentication,
                new String[] { "/customrealm", "kaiser", "yourFancyRealm" });
        Assertions.assertThat(result).isTrue();
    }

    @Test
    public void testHasUidScopeAndCustomRealm() {
        OAuth2Authentication authentication = tokenInfoService.loadAuthentication("123456789");
        ExtendedOAuth2SecurityExpressionMethods methods = new ExtendedOAuth2SecurityExpressionMethods(authentication);
        boolean result = methods.hasUidScopeAndRealm("/customrealm");

        Assertions.assertThat(result).isTrue();
    }

    @Test
    public void testHasUidScopeAndAnyCustomRealm() {
        OAuth2Authentication authentication = tokenInfoService.loadAuthentication("123456789");
        ExtendedOAuth2SecurityExpressionMethods methods = new ExtendedOAuth2SecurityExpressionMethods(authentication);
        boolean result = methods.hasUidScopeAndAnyRealm("/customrealm", "anotherRealm");

        Assertions.assertThat(result).isTrue();
    }

    @Test(expected = AccessDeniedException.class)
    public void testHasUidScopeAndAnyCustomRealmThrowsException() {
        OAuth2Authentication authentication = tokenInfoService.loadAuthentication("123456789");
        ExtendedOAuth2SecurityExpressionMethods methods = new ExtendedOAuth2SecurityExpressionMethods(authentication);
        boolean result = methods.hasUidScopeAndAnyRealm("/notexistentrealm", "doesnotExist");

        Assertions.assertThat(result).isFalse();
        methods.throwOnError(result);
    }

    protected Resource jsonResource(String filename) {
        return new ClassPathResource(filename + ".json", getClass());
    }

    protected String resourceToString(Resource resource) throws IOException {
        return StreamUtils.copyToString(resource.getInputStream(), Charset.defaultCharset());
    }
}
