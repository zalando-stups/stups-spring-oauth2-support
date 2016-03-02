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

import java.net.URI;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;

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
    public void buildRequest() {

        RequestEntity<Void> entity = DefaultTokenInfoRequestExecutor.buildRequestEntity(URI.create(TOKENINFO_URL),
                "0123456789");

    	Assertions.assertThat(entity).isNotNull();
    	
    	Assertions.assertThat(entity.getMethod()).isEqualTo(HttpMethod.GET);
    	Assertions.assertThat(entity.getUrl()).isEqualTo(URI.create(TOKENINFO_URL));
    	
    	Assertions.assertThat(entity.getHeaders()).containsKey(HttpHeaders.AUTHORIZATION);
    	List<String> authorizationHeader = entity.getHeaders().get(HttpHeaders.AUTHORIZATION);
    	Assertions.assertThat(authorizationHeader).containsExactly("Bearer 0123456789");
    	
    	Assertions.assertThat(entity.getHeaders()).containsKey(HttpHeaders.ACCEPT);
    	Assertions.assertThat(entity.getHeaders().getAccept()).contains(MediaType.APPLICATION_JSON);
    }

    @Test(expected = InvalidTokenException.class)
    public void emptyTokenIsInvalid() {
        final TokenInfoResourceServerTokenServices unit = new TokenInfoResourceServerTokenServices(TOKENINFO_URL);
        unit.loadAuthentication("");
    }
}
