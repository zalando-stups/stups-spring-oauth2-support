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
package org.zalando.stups.oauth2.spring.client;

import java.util.Optional;

import org.zalando.stups.tokens.AccessTokens;

/**
 * Uses 'tokens' library from <a href="https://github.com/zalando-stups/tokens">Zalando-STUPS</a>.
 *
 * @author  jbellmann
 */
public class AutoRefreshTokenProvider implements TokenProvider {

    private final String serviceId;
    private final AccessTokens accessTokens;

    public AutoRefreshTokenProvider(final String serviceId, final AccessTokens accessTokens) {
        this.serviceId = serviceId;
        this.accessTokens = accessTokens;
    }

    @Override
    public Optional<String> getToken() {
        return Optional.ofNullable(accessTokens.get(serviceId));
    }

}
