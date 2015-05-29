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

import org.springframework.security.core.context.SecurityContext;

/**
 * Looks in the {@link SecurityContext} for an access_token.
 *
 * @author  jbellmann
 */
public class SecurityContextTokenProvider implements TokenProvider {

    @Override
    public Optional<String> getToken() {
        return AccessTokenUtils.getAccessTokenFromSecurityContext();
    }

}
