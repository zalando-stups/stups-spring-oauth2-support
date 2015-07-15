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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.oauth2.provider.OAuth2Authentication;

/**
 * Implements the 'default'-strategy to get an {@link OAuth2Authentication} as documented in STUPS-Docs.
 *
 * @author  jbellmann
 */
public class DefaultAuthenticationExtractor extends AbstractAuthenticationExtractor {

    private final Logger logger = LoggerFactory.getLogger(DefaultAuthenticationExtractor.class);

    @Override
    protected Set<String> resolveScopes(final Map<String, Object> map) {

        Set<String> scopes = getScopesWithPermissionTrueFromMap(map);

        scopes = validateUidScope(scopes, map);
        return scopes;
    }

    /**
     * Only put scopes with value 'true' into result.
     *
     * @param   map
     *
     * @return  validated scopes
     */
    protected Set<String> getScopesWithPermissionTrueFromMap(final Map<String, Object> map) {
        Set<String> scopes = new HashSet<String>();
        Set<String> permissions = new HashSet<String>();
        try {
            Object scopeValue = map.get("scope");
            if (scopeValue != null) {
                if (scopeValue instanceof ArrayList) {
                    ArrayList<String> scopeValueList = (ArrayList<String>) scopeValue;
                    scopes.addAll(scopeValueList);
                } else {
                    logger.warn("scope-value is {}", scopeValue.getClass().getName());
                }
            }

            // important part, check the scope has the permission, indicated by 'true' as value
            for (String scope : scopes) {
                Object permission = map.get(scope);
                if (permission != null) {
                    if (Boolean.parseBoolean(permission.toString())) {
                        permissions.add(scope);
                    }
                }
            }

        } catch (Exception e) {
            logger.error("Unable to get 'scope' value from map", e);
        }

        return permissions;
    }

}
