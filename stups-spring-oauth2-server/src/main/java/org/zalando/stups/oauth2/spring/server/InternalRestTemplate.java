/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
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