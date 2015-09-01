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
package org.zalando.stups.oauth2.spring.server;

import org.assertj.core.api.Assertions;

import org.junit.Test;

/**
 * @author  jbellmann
 */
public class TokenInfoResourceServerTokenServicesTest {

    private static final String INVALID_TOKENINFO_URL = "someurl.com/tokeninfo";
    private static final String TOKENINFO_URL = "https://someurl.com/tokeninfo";

    @Test
    public void initialize() {
        new TokenInfoResourceServerTokenServices(TOKENINFO_URL);
    }

    @Test(expected = IllegalArgumentException.class)
    public void initializeWithInvalidUrl() {
        new TokenInfoResourceServerTokenServices(INVALID_TOKENINFO_URL);
    }

    @Test(expected = IllegalArgumentException.class)
    public void initializeWithNullUrl() {
        new TokenInfoResourceServerTokenServices(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void initializeWithEmptyUrl() {
        new TokenInfoResourceServerTokenServices("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void initializeWithWhitespaceUrl() {
        new TokenInfoResourceServerTokenServices("  ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void initializeWithNullAuthenticationExtractor() {
        AuthenticationExtractor extractor = null;
        new TokenInfoResourceServerTokenServices(TOKENINFO_URL, extractor);
    }

    @Test(expected = IllegalArgumentException.class)
    public void initializeWithNullRestTemplate() {
        new TokenInfoResourceServerTokenServices(TOKENINFO_URL, "ONLY_A_TEST", new LaxAuthenticationExtractor(), null);
    }

    @Test
    public void buildUrl() {

        String url = TokenInfoResourceServerTokenServices.buildTokenInfoEndpointUrlWithParameter(TOKENINFO_URL,
                "0123456789");
        Assertions.assertThat(url).isNotNull();
        Assertions.assertThat(url).isNotEmpty();
        Assertions.assertThat(url).isEqualTo("https://someurl.com/tokeninfo?access_token=0123456789");
    }
}
