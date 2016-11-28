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

import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.expression.OAuth2ExpressionParser;
import org.springframework.security.oauth2.provider.expression.OAuth2WebSecurityExpressionHandler;
import org.springframework.security.web.FilterInvocation;

/**
 * 
 * @author jbellmann
 *
 */
public class ExtendedOAuth2WebSecurityExpressionHandler extends OAuth2WebSecurityExpressionHandler {

    public ExtendedOAuth2WebSecurityExpressionHandler() {
        setExpressionParser(new OAuth2ExpressionParser(getExpressionParser()));
    }

    @Override
    protected StandardEvaluationContext createEvaluationContextInternal(Authentication authentication,
            FilterInvocation invocation) {
        StandardEvaluationContext ec = super.createEvaluationContextInternal(authentication, invocation);
        ec.setVariable("oauth2", new ExtendedOAuth2SecurityExpressionMethods(authentication));
        return ec;
    }
}
