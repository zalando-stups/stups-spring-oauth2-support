package org.zalando.stups.oauth2.spring.server;

import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

/**
 * 
 * @author jbellmann
 *
 */
public class Issue_38_Test {

	private RestTemplate restOperations;
	private MockRestServiceServer mockServer;

	/**
	 * In case the {@link RestTemplate} has not set the required
	 * {@link ResponseErrorHandler} configured.
	 */
	@Test
	public void withoutTokenInfoResponseErrorHandlerConfigured() {

		// this set by default on RestTemplate
		setUp(new DefaultResponseErrorHandler());

		DefaultTokenInfoRequestExecutor executor = new DefaultTokenInfoRequestExecutor("http://example.com/tokenInfo",
				restOperations);
		try {
			executor.getMap("1234567890");
			Assertions.fail("Expect an HttpClientErrorException");
		} catch (HttpClientErrorException e) {

		}

		mockServer.verify();
	}

	/**
	 * Here the default {@link ResponseErrorHandler} is configured.
	 */
	@Test
	public void withTokenInfoResponseErrorHandlerConfigured() {

		setUp(TokenInfoResponseErrorHandler.getDefault());

		DefaultTokenInfoRequestExecutor executor = new DefaultTokenInfoRequestExecutor("http://example.com/tokenInfo",
				restOperations);

		Map<String, Object> result = executor.getMap("1234567890");
		Assertions.assertThat(result).isNull();

		mockServer.verify();
	}

	protected void setUp(ResponseErrorHandler responseErrorHandler) {
		restOperations = new RestTemplate();
		restOperations.setErrorHandler(responseErrorHandler);
		mockServer = MockRestServiceServer.createServer(restOperations);
		mockServer.expect(MockRestRequestMatchers.requestTo("http://example.com/tokenInfo"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
				.andRespond(MockRestResponseCreators.withUnauthorizedRequest());
	}

}
