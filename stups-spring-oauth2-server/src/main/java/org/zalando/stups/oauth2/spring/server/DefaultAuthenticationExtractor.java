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

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jbellmann
 */
public class DefaultAuthenticationExtractor extends AbstractAuthenticationExtractor {

    private final Logger logger = LoggerFactory.getLogger(DefaultAuthenticationExtractor.class);

    @Override
    protected Set<String> resolveScopes(final Map<String, Object> map) {
        Set<String> scopes = getScopesFromMap(map);
        scopes = validateUidScope(scopes, map);
        return scopes;
    }

    /**
     * Extract scopes from 'scopes'-dictionary.
     *
     * @param map
     *
     * @return scopes
     */
    protected Set<String> getScopesFromMap(final Map<String, Object> map) {
        Set<String> scopes = new HashSet<String>();
        try {
            Object scopeValue = map.get("scope");
            if (scopeValue != null) {
                if (scopeValue instanceof Collection<?>) {
                    scopes.addAll((Collection<String>) scopeValue);
                } else {
                    logger.warn("scope-value is {}", scopeValue.getClass().getName());
                }
            }
        } catch (Exception e) {
            logger.warn("Unable to get 'scope' value from map", e);
        }

        return scopes;
    }

}
