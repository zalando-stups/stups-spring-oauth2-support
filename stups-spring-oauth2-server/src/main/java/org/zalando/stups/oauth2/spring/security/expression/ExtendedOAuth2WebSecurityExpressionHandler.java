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
