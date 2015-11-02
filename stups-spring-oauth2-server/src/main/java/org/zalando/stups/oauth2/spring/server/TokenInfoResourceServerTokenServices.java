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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.BearerTokenExtractor;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

/**
 * This component is used to create an {@link OAuth2Authentication}. Under the
 * hood it takes the 'access_token' from the client-request (done by
 * {@link BearerTokenExtractor} ) and retrieves additional information from the
 * installed 'tokeninfo'-endpoint (https://sec.yourcompany.it/tokeninfo).<br/>
 * Afterwards it extracts 'scope' information and injects these into
 * {@link OAuth2Authentication} object.
 *
 * @author jbellmann
 */
public class TokenInfoResourceServerTokenServices implements ResourceServerTokenServices {

	private static final String ACCESS_TOKEN_PARAM = "?access_token=";

	private static final String CLIENT_ID_NOT_NEEDED = "CLIENT_ID_NOT_NEEDED";

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final String tokenInfoEndpointUrl;

	private final String clientId;

	private final RestTemplate restTemplate;

	private final AuthenticationExtractor authenticationExtractor;

	/**
	 * Specify 'tokenInfoEndpointUrl' to be used by this component.
	 *
	 * @param tokenInfoEndpointUrl
	 */
	public TokenInfoResourceServerTokenServices(final String tokenInfoEndpointUrl) {
		this(tokenInfoEndpointUrl, CLIENT_ID_NOT_NEEDED);
	}

	public TokenInfoResourceServerTokenServices(final String tokenInfoEndpointUrl, final String clientId) {
		this(tokenInfoEndpointUrl, clientId, new LaxAuthenticationExtractor(), buildRestTemplate());
	}

	public TokenInfoResourceServerTokenServices(final String tokenInfoEndpointUrl,
			final AuthenticationExtractor authenticationExtractor) {
		this(tokenInfoEndpointUrl, CLIENT_ID_NOT_NEEDED, authenticationExtractor, buildRestTemplate());
	}

	public TokenInfoResourceServerTokenServices(final String tokenInfoEndpointUrl, final String clientId,
			final AuthenticationExtractor authenticationExtractor, final RestTemplate restTemplate) {

		Assert.hasText(tokenInfoEndpointUrl, "TokenInfoEndpointUrl should never be null or empty");
		try {
			new URL(tokenInfoEndpointUrl);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("TokenInfoEndpointUrl is not an URL", e);
		}

		Assert.hasText(clientId, "ClientId should never be null or empty");
		Assert.notNull(authenticationExtractor, "AuthenticationExtractor should never be null");
		Assert.notNull(restTemplate, "RestTemplate should not be null");

		this.tokenInfoEndpointUrl = tokenInfoEndpointUrl;
		this.authenticationExtractor = authenticationExtractor;
		this.restTemplate = restTemplate;

		this.clientId = clientId;
	}

	@Override
	public OAuth2Authentication loadAuthentication(final String accessToken)
			throws AuthenticationException, InvalidTokenException {

		Map<String, Object> map = null;
		try {

			map = getMap(tokenInfoEndpointUrl, accessToken);
		} catch (Exception e) {

			throw new TokenInfoEndpointException("Retrieving information to 'accessToken' failed.", e);
		}

		if (map.containsKey("error")) {
			logger.debug("userinfo returned error: " + map.get("error"));

			String description = (String) map.get("error_description");
			if (!StringUtils.hasText(description)) {
				description = (String) map.get("error");
			}
			throw new InvalidTokenException(description);
		}

		return this.authenticationExtractor.extractAuthentication(map, clientId);
	}

	@Override
	public OAuth2AccessToken readAccessToken(final String accessToken) {
		throw new UnsupportedOperationException("Not supported: read access token");
	}

	protected Map<String, Object> getMap(final String tokenInfoEndpointUrl, final String accessToken) {
		logger.info("Getting token info from: " + tokenInfoEndpointUrl);

		String urlWithParameter = buildTokenInfoEndpointUrlWithParameter(tokenInfoEndpointUrl, accessToken);
		@SuppressWarnings("rawtypes")
		Map map = restTemplate.getForEntity(urlWithParameter, Map.class).getBody();

		@SuppressWarnings("unchecked")
		Map<String, Object> result = map;
		return result;
	}

	protected static String buildTokenInfoEndpointUrlWithParameter(final String tokenInfoEndpointUrl,
			final String accessToken) {
		StringBuilder sb = new StringBuilder();
		sb.append(tokenInfoEndpointUrl).append(ACCESS_TOKEN_PARAM).append(accessToken);
		return sb.toString();
	}

	public AuthenticationExtractor getAuthenticationExtractor() {
		return this.authenticationExtractor;
	}

	public static RestTemplate buildRestTemplate() {
		RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
		restTemplate.setErrorHandler(new CustomResponseErrorHandler());
		return restTemplate;
	}

	/**
	 * We get an 400 for invalid access-token.
	 * 
	 * @author jbellmann
	 *
	 */
	static class CustomResponseErrorHandler extends DefaultResponseErrorHandler {

		@Override
		protected boolean hasError(HttpStatus statusCode) {
			// we get an 400 for invalid access-token
			if (HttpStatus.BAD_REQUEST.equals(statusCode)) {
				return false;
			}
			return super.hasError(statusCode);
		}
	}

	static class TokenInfoEndpointException extends AuthenticationException {

		private static final long serialVersionUID = 1L;

		private static final String DEFAULT_MESSAGE = "Unknown Exception when calling TokenInfoEndpoint";

		public TokenInfoEndpointException(Throwable t) {
			this(DEFAULT_MESSAGE, t);
		}

		public TokenInfoEndpointException(String msg, Throwable t) {
			super(msg, t);
		}

	}
}
