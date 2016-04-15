package org.zalando.stups.oauth2.spring.server;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.IOException;

class TokenInfoResponseErrorHandler extends DefaultResponseErrorHandler {
    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        if (!response.getStatusCode().is4xxClientError()) {
            super.handleError(response);
        }
    }
}