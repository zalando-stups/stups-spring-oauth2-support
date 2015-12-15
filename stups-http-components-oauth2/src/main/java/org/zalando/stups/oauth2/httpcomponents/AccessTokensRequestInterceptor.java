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
package org.zalando.stups.oauth2.httpcomponents;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;
import org.zalando.stups.tokens.AccessTokens;

import java.io.IOException;

/**
 * @author jbellmann
 */
public class AccessTokensRequestInterceptor implements HttpRequestInterceptor {

    private static final String AUTHORIZATION_HEADER_NAME = "Authorization";
    private static final String BEARER = "Bearer ";
    private final String tokenId;
    private final AccessTokens accessTokens;

    public AccessTokensRequestInterceptor(final String tokenId, final AccessTokens accessTokens) {
        Args.check(tokenId != null, "'tokenId' should never be null");
        Args.check(accessTokens != null, "'accessTokens' should never be null");
        Args.check(!tokenId.trim().isEmpty(), "'tokenId' should never be empty");
        this.tokenId = tokenId;
        this.accessTokens = accessTokens;
    }

    @Override
    public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
        try {
            request.setHeader(AUTHORIZATION_HEADER_NAME, getBearerToken());
        } catch (Exception e) {
            throw new HttpException("Unable to place header 'access_token' into request.", e);
        }
    }

    /**
     * Return the Bearer token header.
     *
     * @return The Bearer access_token
     */
    private String getBearerToken() {
        // Using String concatenation because it's short and out of a loop. The compiler will turn it to StringBuilder
        return BEARER + this.accessTokens.get(this.tokenId);
    }

}
