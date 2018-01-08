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

import net.jodah.failsafe.CircuitBreaker;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.springframework.util.Assert;
import org.springframework.web.client.RestOperations;

import java.util.Map;
import java.util.concurrent.Callable;

public class FailsafeTokenInfoRequestExecutor extends DefaultTokenInfoRequestExecutor {

  private static Map<String, Object> FALLBACK = null;

  private final CircuitBreaker circuitBreaker;

  private final RetryPolicy retryPolicy;

  public FailsafeTokenInfoRequestExecutor(
      final String tokenInfoEndpointUrl,
      final CircuitBreaker circuitBreaker,
      final RetryPolicy retryPolicy) {
    this(tokenInfoEndpointUrl, buildRestTemplate(), circuitBreaker, retryPolicy);
  }

  public FailsafeTokenInfoRequestExecutor(
      final String tokenInfoEndpointUrl,
      final RestOperations restOperations,
      final CircuitBreaker circuitBreaker,
      final RetryPolicy retryPolicy) {
    super(tokenInfoEndpointUrl, restOperations);

    Assert.notNull(circuitBreaker, "'circuitBreaker' should never be null");
    Assert.notNull(retryPolicy, "'retryPolicy' should never be null");

    this.circuitBreaker = circuitBreaker;
    this.retryPolicy = retryPolicy;
  }

  @Override
  public Map<String, Object> getMap(final String accessToken) {
    Callable<Map<String, Object>> callable = new Callable<Map<String, Object>>() {
      @Override
      public Map<String, Object> call() throws Exception {
        return FailsafeTokenInfoRequestExecutor.super.getMap(accessToken);
      }
    };
    return Failsafe
        .with(retryPolicy)
        .with(circuitBreaker)
        .withFallback(FALLBACK)
        .get(callable);
  }
}
