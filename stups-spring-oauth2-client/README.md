## How to use this library

This library provides a subclass of Spring's RestTemplate, which is capable of adding the OAuth2 Authorization bearer
header to eahc request. The StupsOAuth2RestTemplate requires an AccessTokenProvider to obtain the current token.
Here is an example how to wire everything together:

```java

    @Autowired
    private AccessTokens accessTokens;

    @Bean
    public KioOperations kioOperations(@Value("${kio.url}") String kioBaseUrl) {
        return new RestTemplateKioOperations(buildOAuth2RestTemplate("kio"), kioBaseUrl);
    }

    @Bean
    public PieroneOperations pieroneOperations(@Value("${pierone.url}") String pieroneBaseUrl) {
        return new RestTemplatePieroneOperations(buildOAuth2RestTemplate("pierone"), pieroneBaseUrl);
    }

    private RestOperations buildOAuth2RestTemplate(final String tokenName) {
        return new StupsOAuth2RestTemplate(new StupsTokensAccessTokenProvider(tokenName, accessTokens));
    } 
     
```


## AccessTokenProviders

Let's have a look at the AccessTokenProviders


### StupsTokensAccessTokenProvider

This is a wrapper for the [Stups' tokens library](https://github.com/zalando-stups/tokens), which is able to obtain
a list of access tokens, using given credentials, and each having different scopes.

It is useful when your application/(micro)service needs to access OAuth2-protected resources with its own service user, 
e.g. like in background-tasks, jobs, etc.

To get this easily to work in a Spring-Boot application you should have a look at
[spring-boot-zalando-stups-tokens](https://github.com/zalando-stups/spring-boot-zalando-stups-tokens).

### SecurityContextTokenProvider

This implementation is useful when your application/(micro)service is somewhere in between of a request-flow initiated
by a 'real' user or another service that are already authenticated and authorized when it (the request) goes through
your application (you have security in place, right?).

Then we take the access-token from Springs SecurityContext (from the incoming request) to make succeeding requests. 

#### How to implement clients with Springs-RestTemplate?

Maybe you will have a look at our [kio-client-java](https://github.com/zalando-stups/kio-client-java) to easily use your client-implementation with or without OAuth2 working.

Also have a look at [riptide](https://github.com/zalando/riptide). Looks amazing. ;-)