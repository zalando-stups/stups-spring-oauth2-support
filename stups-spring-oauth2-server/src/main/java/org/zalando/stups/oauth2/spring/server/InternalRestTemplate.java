package org.zalando.stups.oauth2.spring.server;

import java.io.IOException;
import java.net.URI;

import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * 
 * @author jbellmann
 *
 */
class InternalRestTemplate extends RestTemplate {

	InternalRestTemplate(ClientHttpRequestFactory requestFactory) {
		super(requestFactory);
	}

	// TO AVOID URL WITH SENSITIVE INFORMATION SHOWN IN LOGS
	@Override
	protected <T> T doExecute(URI url, HttpMethod method, RequestCallback requestCallback,
			ResponseExtractor<T> responseExtractor) throws RestClientException {
		try {
			return super.doExecute(url, method, requestCallback, responseExtractor);
		} catch (ResourceAccessException e) {
			// skip the original message, take from original IOException and
			// see what happens
			throw new ResourceAccessException(e.getCause().getMessage(), (IOException) e.getCause());
		}
	}
}