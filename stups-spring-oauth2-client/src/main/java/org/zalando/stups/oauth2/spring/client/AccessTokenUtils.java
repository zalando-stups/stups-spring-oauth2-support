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
package org.zalando.stups.oauth2.spring.client;

import static org.springframework.security.oauth2.common.OAuth2AccessToken.ACCESS_TOKEN;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import com.google.common.base.Optional;

/**
 * Utilities to work with AccessTokens and SecurityContext.
 *
 * @author  jbellmann
 */
public class AccessTokenUtils {

    private AccessTokenUtils() {
        //
    }

    public static Optional<String> getAccessTokenFromSecurityContext() {
        SecurityContext securityContext = SecurityContextHolder.getContext();

        Authentication authentication = securityContext.getAuthentication();
        if (authentication instanceof OAuth2Authentication) {
            Object userDetails = ((OAuth2Authentication) authentication).getUserAuthentication().getDetails();
            if (userDetails != null) {
                try {
                    final Map details = (Map) userDetails;
                    return Optional.fromNullable((String) details.get(ACCESS_TOKEN));
                } catch (ClassCastException e) {

                    return Optional.absent();
                }
            } else {

                return Optional.absent();
            }
        }

        return Optional.absent();
    }
}
