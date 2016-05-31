package org.zalando.stups.oauth2.spring.server;

import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
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

	@Before
	public void setUp() {

		restOperations = new RestTemplate();
		restOperations.setErrorHandler(TokenInfoResponseErrorHandler.getDefault());
		mockServer = MockRestServiceServer.createServer(restOperations);

		mockServer.expect(MockRestRequestMatchers.requestTo("http://example.com/tokenInfo"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
				.andRespond(MockRestResponseCreators.withUnauthorizedRequest());
	}

	/**
	 * In case the {@link RestTemplate} has not set the required {@link ResponseErrorHandler} configured.
	 */
	@Test
	public void withoutTokenInfoResponseErrorHandlerConfigured() {

		restOperations = new RestTemplate();
		// no error-handler
		mockServer = MockRestServiceServer.createServer(restOperations);
		mockServer.expect(MockRestRequestMatchers.requestTo("http://example.com/tokenInfo"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
				.andRespond(MockRestResponseCreators.withUnauthorizedRequest());

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

		restOperations = new RestTemplate();
		restOperations.setErrorHandler(TokenInfoResponseErrorHandler.getDefault());
		mockServer = MockRestServiceServer.createServer(restOperations);
		mockServer.expect(MockRestRequestMatchers.requestTo("http://example.com/tokenInfo"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
				.andRespond(MockRestResponseCreators.withUnauthorizedRequest());

		DefaultTokenInfoRequestExecutor executor = new DefaultTokenInfoRequestExecutor("http://example.com/tokenInfo",
				restOperations);

		Map<String, Object> result = executor.getMap("1234567890");
		Assertions.assertThat(result).isNull();

		mockServer.verify();
	}

}
