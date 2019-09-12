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
package some.test.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.DefaultOAuth2ExceptionRenderer;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;
import org.springframework.security.oauth2.provider.error.OAuth2AuthenticationEntryPoint;
import org.springframework.security.oauth2.provider.error.OAuth2ExceptionRenderer;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.web.client.ResourceAccessException;
import org.zalando.stups.oauth2.spring.security.expression.ExtendedOAuth2WebSecurityExpressionHandler;
import org.zalando.stups.oauth2.spring.server.DefaultAuthenticationExtractor;
import org.zalando.stups.oauth2.spring.server.DefaultTokenInfoRequestExecutor;
import org.zalando.stups.oauth2.spring.server.TokenInfoRequestExecutor;
import org.zalando.stups.oauth2.spring.server.TokenInfoResourceServerTokenServices;
import org.zalando.stups.oauth2.spring.server.TokenResponseErrorHandler;

/**
 * Configures the Resource-Server. We want the resources under '/secure/**'
 * secured by OAuth2 and the needed scope is 'testscope'.
 *
 * @author jbellmann
 */
@Configuration
@EnableResourceServer
public class OAuthConfiguration extends ResourceServerConfigurerAdapter {

    @Value("${spring.oauth2.resource.tokenInfoUri}")
    private String tokenInfoUri;

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        OAuth2ExceptionRenderer exceptionRenderer = new DefaultOAuth2ExceptionRenderer();

        final OAuth2AuthenticationEntryPoint authenticationEntryPoint = new OAuth2AuthenticationEntryPoint();
        authenticationEntryPoint.setExceptionRenderer(exceptionRenderer);

        final OAuth2AccessDeniedHandler accessDeniedHandler = new OAuth2AccessDeniedHandler();
        accessDeniedHandler.setExceptionRenderer(exceptionRenderer);

        resources.authenticationEntryPoint(authenticationEntryPoint).accessDeniedHandler(accessDeniedHandler);
        // here is the important part
        resources.expressionHandler(new ExtendedOAuth2WebSecurityExpressionHandler());
    }

    /**
     * Configure scopes for specific controller/httpmethods/roles here.
     */
    @Override
    public void configure(final HttpSecurity http) throws Exception {

        // @formatter:off
        http
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.NEVER)
            .and()
                .authorizeRequests()
                    .antMatchers(HttpMethod.GET, "/secured/**").access("#oauth2.hasScope('testscope')")
                    .antMatchers(HttpMethod.GET, "/realmSecured/**").access("#oauth2.hasRealm('/customrealm')")
                    .antMatchers(HttpMethod.GET, "/combinedRealmSecured/**").access("#oauth2.hasUidScopeAndRealm('/customrealm')");
        // @formatter:on
    }

    @Profile("defaultAuthentication")
    @Bean
    public ResourceServerTokenServices customResourceTokenServices() {

        return new TokenInfoResourceServerTokenServices(tokenInfoUri, new DefaultAuthenticationExtractor());
    }

    @Profile("tokeninfoTimeout")
    @Bean
    public ResourceServerTokenServices tokeninfoTimeoutResourceTokenServices() {
        return new TokenInfoResourceServerTokenServices(new TokenInfoRequestExecutor() {
            private final TokenInfoRequestExecutor delegate = new DefaultTokenInfoRequestExecutor(tokenInfoUri, DefaultTokenInfoRequestExecutor.buildRestTemplate(TokenResponseErrorHandler.getDefault()));

            @Override
            public Map<String, Object> getMap(String accessToken) {
                try {
                    return delegate.getMap(accessToken);
                } catch (ResourceAccessException e) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("error", "Read timed out");
                    return map;
                }
            }
        });
    }

    /**
     * @return
     *
     * @deprecated lax will become the new default
     */
    @Deprecated
    @Profile("laxAuthentication")
    @Bean
    public ResourceServerTokenServices laxResourceTokenServices() {

        return new TokenInfoResourceServerTokenServices(tokenInfoUri, new DefaultAuthenticationExtractor());
    }

    static class TimeoutHandlingRequestExecutor implements TokenInfoRequestExecutor {

        private final TokenInfoRequestExecutor delegate;

        public TimeoutHandlingRequestExecutor(TokenInfoRequestExecutor delegate) {
            this.delegate = delegate;
        }

        @Override
        public Map<String, Object> getMap(String accessToken) {
            try {
                return delegate.getMap(accessToken);
            } catch (ResourceAccessException e) {
                Map<String, Object> map = new HashMap<>();
                map.put("error", "Read timed out");
                return map;
            }
        }
    }
}
