package org.zalando.stups.oauth2.spring.server;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import org.springframework.web.client.RestOperations;

import java.util.Map;

public class HystrixTokenInfoRequestExecutor extends DefaultTokenInfoRequestExecutor {

  public HystrixTokenInfoRequestExecutor(String tokenInfoEndpointUrl) {
    super(tokenInfoEndpointUrl);
  }

  public HystrixTokenInfoRequestExecutor(
      final String tokenInfoEndpointUrl,
      final RestOperations restOperations) {
    super(tokenInfoEndpointUrl, restOperations);
  }

  @Override
  public final Map<String, Object> getMap(final String accessToken) {
    return new TokenInfoCommand(this, accessToken).execute();
  }

  static class TokenInfoCommand extends HystrixCommand<Map<String, Object>> {

    private final HystrixTokenInfoRequestExecutor tokenInfoRequestExecutor;

    private final String accessToken;

    TokenInfoCommand(
        final HystrixTokenInfoRequestExecutor tokenInfoRequestExecutor,
        final String accessToken) {
      super(HystrixCommandGroupKey.Factory.asKey("TokenInfo"));
      this.tokenInfoRequestExecutor = tokenInfoRequestExecutor;
      this.accessToken = accessToken;
    }

    @Override
    protected Map<String, Object> run() throws Exception {
      return tokenInfoRequestExecutor.doGetMap(accessToken);
    }

    @Override
    protected Map<String, Object> getFallback() {
      return null;
    }
  }
}
