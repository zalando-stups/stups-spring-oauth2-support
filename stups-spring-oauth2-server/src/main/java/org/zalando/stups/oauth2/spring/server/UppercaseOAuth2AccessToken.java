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

import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

/**
 * Because {@link #getTokenType()} is used in the header it should return 'Bearer' instead of 'bearer'.
 *
 * @author  jbellmann
 */
public class UppercaseOAuth2AccessToken extends DefaultOAuth2AccessToken {

    private static final long serialVersionUID = 1L;

    public UppercaseOAuth2AccessToken(final String value) {
        super(value);
    }

    @Override
    public String getTokenType() {
        return OAuth2AccessToken.BEARER_TYPE;
    }

}
