### How to use

```
...
        HttpRequestInterceptor interceptor = new AccessTokensRequestInterceptor(SERVICE_ID, accessTokens);
        HttpClient client = HttpClientBuilder.create().addInterceptorFirst(interceptor).build();

...
```

A complete working example is available in the tests.

