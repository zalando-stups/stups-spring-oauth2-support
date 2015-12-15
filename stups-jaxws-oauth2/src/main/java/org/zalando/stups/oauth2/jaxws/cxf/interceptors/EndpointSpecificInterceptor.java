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
package org.zalando.stups.oauth2.jaxws.cxf.interceptors;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.zalando.stups.tokens.AccessTokens;

import java.util.HashSet;
import java.util.Set;

public class EndpointSpecificInterceptor extends OAuth2TokenInterceptor {

    private final Set<String> endpointNames;

    private EndpointSpecificInterceptor(final Builder builder) {
        super(builder.accessTokens, builder.serviceId); // Checks performed in superclass.

        check(builder.endpointNames != null, "Set of endpoint names should not be null.");
        check(!builder.endpointNames.isEmpty(), "Set of endpoint names should not be empty.");

        for (final String name : builder.endpointNames) {
            check(isOk(name), "Endpoint name must be non-null and non-empty.");
        }

        this.endpointNames = builder.endpointNames;
    }

    @Override
    public void handleMessage(final Message message) throws Fault {

        final String endpointAddress = String.valueOf(message.get(Message.ENDPOINT_ADDRESS));

        for (final String name : endpointNames) {
            if (endpointAddress.contains(name)) {
                super.handleMessage(message);
            }
        }
    }

    private boolean isOk(final String string) {
        return string != null && !string.isEmpty() && !string.trim().isEmpty();
    }

    public static final class Builder {
        private AccessTokens accessTokens;
        private String serviceId;
        private final Set<String> endpointNames = new HashSet<>();

        public Builder accessTokens(final AccessTokens accessTokens) {
            this.accessTokens = accessTokens;
            return this;
        }

        public Builder serviceId(final String serviceId) {
            this.serviceId = serviceId;
            return this;
        }

        public Builder endpointName(final String name) {
            this.endpointNames.add(name);
            return this;
        }

        public EndpointSpecificInterceptor build() {
            return new EndpointSpecificInterceptor(this);
        }

    }

}
