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

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.expression.OAuth2ExpressionUtils;

/**
 * 
 * @author jbellmann
 *
 */
public abstract class RealmOAuth2ExpressionUtils extends OAuth2ExpressionUtils {

    public static boolean hasAnyRealm(Authentication authentication, String[] realms) {

        if (authentication instanceof OAuth2Authentication) {
            Object details = ((OAuth2Authentication) authentication).getUserAuthentication().getDetails();
            if (details instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) details;
                Object realmFromMap = map.get("realm");
                if (realmFromMap != null && realmFromMap instanceof String) {
                    String realm = (String) realmFromMap;
                    for (String s : realms) {
                        if (realm.equals(s)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }
}
