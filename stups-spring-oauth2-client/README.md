###Configure AccessTokenProvider

This module provides an implementation of AccessTokenProvider:

```
        BaseOAuth2ProtectedResourceDetails resource = new BaseOAuth2ProtectedResourceDetails();
        resource.setClientId("what_here");

        restTemplate = new OAuth2RestTemplate(resource);

        // here is the token-provider
        restTemplate.setAccessTokenProvider(new StupsTokensAccessTokenProvider("kio", accessTokens));
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());

        client = new RestTemplateKioOperations(restTemplate, baseUrl);
```

