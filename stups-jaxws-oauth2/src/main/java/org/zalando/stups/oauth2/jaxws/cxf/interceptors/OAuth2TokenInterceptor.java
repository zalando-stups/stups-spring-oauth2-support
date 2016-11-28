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
package org.zalando.stups.oauth2.jaxws.cxf.interceptors;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

import org.zalando.stups.tokens.AccessTokens;

/**
 * Simple {@link Interceptor} that retrieves a token from {@link AccessTokens} and put it into the header.
 *
 * @author  jbellmann
 */
public class OAuth2TokenInterceptor extends AbstractPhaseInterceptor<Message> {

    private static final String AUTHORIZATION_HEADER_NAME = "Authorization";
    private static final String BEARER = "Bearer ";
    private final AccessTokens accessTokens;
    private final String serviceId;

    public OAuth2TokenInterceptor(final AccessTokens accessTokens, final String serviceId) {
        super(Phase.PRE_STREAM);

        check(accessTokens != null, "'AccessTokens' should never be null");
        check(serviceId != null, "'serviceId' should never be null");
        check(!serviceId.trim().isEmpty(), "'serviceId' should never be empty");

        this.accessTokens = accessTokens;
        this.serviceId = serviceId;
    }

    @Override
    public void handleMessage(final Message message) throws Fault {

        @SuppressWarnings("unchecked")
        Map<String, List<String>> headers = (Map<String, List<String>>) message.get(Message.PROTOCOL_HEADERS);

        if (headers == null) {
            headers = new TreeMap<String, List<String>>();
            message.put(Message.PROTOCOL_HEADERS, headers);
        }

        headers.put(AUTHORIZATION_HEADER_NAME, Collections.singletonList(getHeaderValue()));
    }

    protected String getHeaderValue() {
        return BEARER + accessTokens.get(serviceId);
    }

    public static void check(final boolean expression, final String message) {
        if (!expression) {
            throw new IllegalArgumentException(message);
        }
    }
}
