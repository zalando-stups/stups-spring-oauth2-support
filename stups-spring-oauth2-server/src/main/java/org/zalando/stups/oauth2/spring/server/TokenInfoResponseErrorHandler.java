package org.zalando.stups.oauth2.spring.server;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import java.io.IOException;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

class TokenInfoResponseErrorHandler extends DefaultResponseErrorHandler {
    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        if (response.getStatusCode() != BAD_REQUEST) {
            super.handleError(response);
        }
    }
}