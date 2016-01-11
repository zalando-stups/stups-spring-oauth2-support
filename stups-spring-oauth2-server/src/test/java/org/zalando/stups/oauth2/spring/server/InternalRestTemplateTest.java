package org.zalando.stups.oauth2.spring.server;

import java.io.IOException;
import java.net.URI;

import org.assertj.core.api.Assertions;
import org.assertj.core.util.Maps;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

public class InternalRestTemplateTest {

	private static final String URL = "http://172.34.23.12";
	private static final String JUST_FOR_TESTING = "JUST FOR TESTING";

	@Test
	public void testSocketTimeoutException() {
		
		ResourceAccessException targetException = null;

		RestTemplate restTemplate = new TestInternalRestTemplate(new HttpComponentsClientHttpRequestFactory());
		try {
			restTemplate.execute(URL, HttpMethod.GET, Mockito.mock(RequestCallback.class), Mockito.mock(ResponseExtractor.class), Maps.newConcurrentHashMap());
		} catch (ResourceAccessException e) {
			targetException = e;
		}
		
		Assertions.assertThat(targetException.getMessage()).startsWith(JUST_FOR_TESTING);
		Assertions.assertThat(targetException.getMessage()).doesNotContain(URL);
		Assertions.assertThat(targetException.getCause().getMessage()).startsWith(JUST_FOR_TESTING);
		Assertions.assertThat(targetException.getCause().getMessage()).doesNotContain(URL);
	}

	static class TestInternalRestTemplate extends InternalRestTemplate {

		TestInternalRestTemplate(ClientHttpRequestFactory requestFactory) {
			super(requestFactory);
		}

		@Override
		protected ClientHttpRequest createRequest(URI url, HttpMethod method) throws IOException {
			ClientHttpRequest request = Mockito.mock(ClientHttpRequest.class);
			Mockito.when(request.execute()).thenThrow(new IOException(JUST_FOR_TESTING));
			return request;
		}

	}

}
