package org.zalando.stups.oauth2.spring.server;

import static com.google.common.collect.Lists.newArrayList;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.Assert;
import org.springframework.web.client.DefaultResponseErrorHandler;

import com.google.common.collect.ImmutableList;

class TokenInfoResponseErrorHandler extends DefaultResponseErrorHandler {

    private final List<HttpStatus> statusList;

    TokenInfoResponseErrorHandler(List<HttpStatus> statusList) {
        Assert.notNull(statusList, "'statusList' should never be null");
        this.statusList = ImmutableList.copyOf(statusList);
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        if (!statusList.contains(response.getStatusCode())) {
            super.handleError(response);
        }
    }

    static TokenInfoResponseErrorHandler getDefault() {
        return new TokenInfoResponseErrorHandler(
                newArrayList(BAD_REQUEST, UNAUTHORIZED, FORBIDDEN));
    }

    protected List<HttpStatus> getStatusList() {
        return statusList;
    }
}