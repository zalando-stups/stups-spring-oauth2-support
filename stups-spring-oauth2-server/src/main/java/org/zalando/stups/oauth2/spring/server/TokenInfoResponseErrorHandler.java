package org.zalando.stups.oauth2.spring.server;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import java.io.IOException;
import java.util.EnumSet;

import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.Assert;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;

class TokenInfoResponseErrorHandler extends DefaultResponseErrorHandler {

    private final EnumSet<HttpStatus> unhandledStatusSet;

    TokenInfoResponseErrorHandler(EnumSet<HttpStatus> unhandledStatusSet) {
        Assert.notNull(unhandledStatusSet, "'statusList' should never be null");
        this.unhandledStatusSet = unhandledStatusSet;
    }

    /**
     * Delegates only to {@link DefaultResponseErrorHandler} when
     * {@link HttpStatus} of {@link ClientHttpResponse} is not in
     * 'unhandledStatusSet'.
     * 
     * @see #getDefault() for more details about which {@link HttpStatus} will
     *      be not handled by default
     */
    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        if (!unhandledStatusSet.contains(response.getStatusCode())) {
            super.handleError(response);
        }
    }

    /**
     * Creates an instance of {@link TokenInfoResponseErrorHandler} with
     * prepared set of {@link HttpStatus}-codes not handled by this
     * {@link ResponseErrorHandler}.<br/>
     * Not handled by default are {@link HttpStatus#BAD_REQUEST},
     * {@link HttpStatus#UNAUTHORIZED} and {@link HttpStatus#FORBIDDEN}
     * 
     * @return
     */
    static TokenInfoResponseErrorHandler getDefault() {
        return new TokenInfoResponseErrorHandler(
                EnumSet.of(BAD_REQUEST, UNAUTHORIZED, FORBIDDEN));
    }

    protected EnumSet<HttpStatus> getUnhandledStatusSet() {
        return unhandledStatusSet;
    }
}