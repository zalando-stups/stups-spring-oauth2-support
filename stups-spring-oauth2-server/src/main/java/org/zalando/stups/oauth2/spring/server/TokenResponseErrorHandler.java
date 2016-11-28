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
package org.zalando.stups.oauth2.spring.server;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import java.io.IOException;

import java.util.EnumSet;

import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

import org.springframework.util.Assert;

import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;

public class TokenResponseErrorHandler extends DefaultResponseErrorHandler {

    private final EnumSet<HttpStatus> unhandledStatusSet;

    TokenResponseErrorHandler(final EnumSet<HttpStatus> unhandledStatusSet) {
        Assert.notNull(unhandledStatusSet, "'statusList' should never be null");
        this.unhandledStatusSet = unhandledStatusSet;
    }

    /**
     * Delegates only to {@link DefaultResponseErrorHandler} when {@link HttpStatus} of {@link ClientHttpResponse} is
     * not in 'unhandledStatusSet'.
     *
     * @see  #getDefault() for more details about which {@link HttpStatus} will be not handled by default
     */
    @Override
    public void handleError(final ClientHttpResponse response) throws IOException {
        if (!unhandledStatusSet.contains(response.getStatusCode())) {
            super.handleError(response);
        }
    }

    /**
     * Creates an instance of {@link TokenResponseErrorHandler} with prepared set of {@link HttpStatus}-codes not
     * handled by this {@link ResponseErrorHandler}.<br/>
     * Not handled by default are {@link HttpStatus#BAD_REQUEST}, {@link HttpStatus#UNAUTHORIZED} and
     * {@link HttpStatus#FORBIDDEN}
     *
     * @return
     */
    public static TokenResponseErrorHandler getDefault() {
        return new TokenResponseErrorHandler(EnumSet.of(BAD_REQUEST, UNAUTHORIZED, FORBIDDEN));
    }

    protected EnumSet<HttpStatus> getUnhandledStatusSet() {
        return unhandledStatusSet;
    }
}
