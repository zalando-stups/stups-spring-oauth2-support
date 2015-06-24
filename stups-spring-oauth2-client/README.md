### How to use this library

Maybe it is enough to show you some code. Here is a snippet from the [fullstop-Application](https://github.com/zalando-stups/fullstop) where we use client-implementations to get some information from [kio](https://github.com/zalando-stups/kio) and [pierone](https://github.com/zalando-stups/pierone).


```
...

    @Bean
    public KioOperations kioOperations() {
        return new RestTemplateKioOperations(buildOAuth2RestTemplate("kio"), kioBaseUrl);
    }

    @Bean
    public PieroneOperations pieroneOperations() {
        return new RestTemplatePieroneOperations(buildOAuth2RestTemplate("pierone"), pieroneBaseUrl);
    }

    private RestOperations buildOAuth2RestTemplate(String tokenName) {
        final BaseOAuth2ProtectedResourceDetails resource = new BaseOAuth2ProtectedResourceDetails();
        // we call these services from 'fullstop', so we put our application name here
        resource.setClientId("fullstop");

        // because we want to do OAuth2, we use the OAuth2RestTemplate implementation
        // provided with the 'spring-security-oauth2' artifact
        final OAuth2RestTemplate restTemplate = new OAuth2RestTemplate(resource);

        // to get an 'AccessToken' into the headers of every request we had to implement 'AccessTokenProvider'
        // what we called 'StupsAccessTokenProvider', to be flexible we implemented it as a chain, so you are able
        // to chain them if you need, read more about that below
        restTemplate.setAccessTokenProvider(new StupsAccessTokenProvider(new AutoRefreshTokenProvider(tokenName, accessTokens)));

        // you are free to choose whatever 'RequestFactory' you want to use
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());

        return restTemplate;
    }
```

[full code of ClientConfig](https://github.com/zalando-stups/fullstop/blob/master/fullstop/src/main/java/org/zalando/stups/fullstop/config/ClientConfig.java)

#### Some more words about 'StupsAccessTokenProvider'

As written above it's our implementation of 'AccessTokenProvider' what is used internally by OAuth2RestTemplate to get an
access-token what is then placed in a request-header.

To make the implementation flexible we defined our own interface [TokenProvider](https://github.com/zalando-stups/stups-spring-oauth2-support/blob/master/stups-spring-oauth2-client/src/main/java/org/zalando/stups/oauth2/spring/client/TokenProvider.java) that is very simple. Currently we have two implementations for 'TokenProvider':

##### AutoRefreshTokenProvider

This [implementation](https://github.com/zalando-stups/stups-spring-oauth2-support/blob/master/stups-spring-oauth2-client/src/main/java/org/zalando/stups/oauth2/spring/client/AutoRefreshTokenProvider.java) is useful when your application/(micro)service is doing some background-tasks, jobs or something like that where
no 'real' user initiated an action on your application. AutoRefreshTokenProvider uses the whole [mint](http://stups.readthedocs.org/en/latest/components/mint.html) and [berry](http://stups.readthedocs.org/en/latest/components/berry.html) machinery with rotating client-credentials mounted from S3 into your Docker-Container.

To get this easily to work in a Spring-Boot application you should have a look at [spring-boot-zalando-stups-tokens](https://github.com/zalando-stups/spring-boot-zalando-stups-tokens).

##### SecurityContextTokenProvider

This [implementation](https://github.com/zalando-stups/stups-spring-oauth2-support/blob/master/stups-spring-oauth2-client/src/main/java/org/zalando/stups/oauth2/spring/client/SecurityContextTokenProvider.java) is useful when your application/(micro)service is somewhere in between of an request-flow initiated by an
'real' user or another service that are already authenticated and authorized when it (the request) goes through your application (you have security in place, right?). Then we take the access-token from Springs SecurityContext to make the next server-hop when requesting another service via http.