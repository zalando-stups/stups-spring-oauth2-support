package org.zalando.stups.oauth2.spring.security.expression;

import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.common.util.OAuth2Utils;

import java.util.Set;

public class InsufficientRealmException extends OAuth2Exception {

    public InsufficientRealmException(String msg, Set<String> validRealms) {
        this(msg);
        addAdditionalInformation("realm", OAuth2Utils.formatParameterList(validRealms));
    }

    public InsufficientRealmException(String msg) {
        super(msg);
    }

    @Override
    public int getHttpErrorCode() {
        return 403;
    }

    @Override
    public String getOAuth2ErrorCode() {
        // Not defined in the spec, so not really an OAuth2Exception
        return "insufficient_realm";
    }
}
