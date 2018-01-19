### Configure your ResourceServerTokenServices-Bean

This library provides an implementation of ResourceServerTokenServices.

```
    @Bean
    public ResourceServerTokenServices customResourceTokenServices() {

        return new TokenInfoResourceServerTokenServices(tokenInfoUri);
    }
```

Or some more code:

```
@Configuration
@EnableResourceServer
public class OAuthConfiguration extends ResourceServerConfigurerAdapter {

    @Value("${spring.oauth2.resource.tokenInfoUri}")
    private String tokenInfoUri;

    /**
     * Configure scopes for specific controller/httpmethods/roles here.
     */
    @Override
    public void configure(final HttpSecurity http) throws Exception {

        //J-
        http
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.NEVER)
            .and()
                .authorizeRequests()
                    .antMatchers(HttpMethod.GET, "/secured/**").access("#oauth2.hasScope('testscope')");
        //J+
    }

    @Bean
    public ResourceServerTokenServices customResourceTokenServices() {

        return new TokenInfoResourceServerTokenServices(tokenInfoUri);
    }

}
```

You can also configure a HttpUserRolesProvider which will allow more advanced spring security access policies.


```
@Configuration
@EnableResourceServer
public class OAuthConfiguration extends ResourceServerConfigurerAdapter {

    @Value("${spring.oauth2.resource.tokenInfoUri}")
    private String tokenInfoUri;
    
    @Value("${spring.oauth2.resource.roleInfoUri}")
    private String roleInfoUri;

    /**
     * Configure scopes for specific controller/httpmethods/roles here.
     */
    @Override
    public void configure(final HttpSecurity http) throws Exception {

        //J-
        http
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.NEVER)
            .and()
                .authorizeRequests()
                    .antMatchers(HttpMethod.GET, "/secured/**")
                    .access("#oauth2.hasScope('testscope1') or (#oauth2.hasScope('testscope2') and hasAuthority('testRole'))");
        //J+
    }

    @Bean
    public ResourceServerTokenServices customResourceTokenServices() {
        final HttpUserRolesProvider httpUserRolesProvider = new HttpUserRolesProvider(roleInfoUri);
        return new TokenInfoResourceServerTokenServices(tokenInfoUri, clientId, httpUserRolesProvider);
    }
}
```

Using your favourite Circuit breaker (Currently Hystrix and Failsafe are supported)
```
    @Bean
    public ResourceServerTokenServices customResourceTokenServices() {
        return new TokenInfoResourceServerTokenServices(new HystrixTokenInfoRequestExecutor(tokenInfoUri));
    }

```

or


```
    @Bean
    public ResourceServerTokenServices customResourceTokenServices() {
        CircuitBreaker breaker = new CircuitBreaker()
          .withFailureThreshold(3, 10)
          .withSuccessThreshold(5)
          .withDelay(1, TimeUnit.MINUTES);

        RetryPolicy retryPolicy = new RetryPolicy()
          .retryOn(Exception.class, IOException.class)
          .retryOn(failure -> failure instanceof ConnectException);


        return new TokenInfoResourceServerTokenServices(new FailsafeTokenInfoRequestExecutor(tokenInfoUri,breaker,retryPolicy));
    }

```
