### How to use

```

HttpRequestInterceptor interceptor = new AccessTokensRequestInterceptor(SERVICE_ID, accessTokens);
HttpClient client = HttpClientBuilder.create().addInterceptorFirst(interceptor).build();

```

A complete working example is available in the [tests](https://github.com/zalando-stups/stups-spring-oauth2-support/blob/master/stups-http-components-oauth2/src/test/java/org/zalando/stups/oauth2/httpcomponents/RoundtripTest.java).

