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
