package org.zalando.stups.oauth2.spring.server;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.util.Assert;

/**
 * Utility to create wrapped {@link TokenInfoRequestExecutor} to translate
 * exceptions thrown during Token-Info-Request.
 * 
 * @author jbellmann
 *
 */
public class ExecutorWrappers {

    private static final String ACCESS_TOKEN_NOT_VALID = "Access Token not valid";

    /**
     * Wraps the provided {@link TokenInfoRequestExecutor} to re-throw any
     * {@link RuntimeException} thrown as an {@link InvalidTokenException}
     * except {@link OAuth2Exception}s (not subclasses). Any concrete
     * {@link OAuth2Exception}s are re-thrown as is.
     * 
     * @param tokenInfoRequestExecutor
     * @return
     */
    public static TokenInfoRequestExecutor wrap(TokenInfoRequestExecutor tokenInfoRequestExecutor) {
        return new RequestExecutorWrapper(tokenInfoRequestExecutor, rethrowExceptionsAsList(OAuth2Exception.class),
                new InvalidTokenException(ACCESS_TOKEN_NOT_VALID));
    }

    /**
     * Wraps the provided {@link TokenInfoRequestExecutor} to re-throw any
     * {@link RuntimeException} thrown as the provided {@link OAuth2Exception}
     * -instance. Any concrete {@link OAuth2Exception} (not subclasses) is
     * re-thrown as is.
     * 
     * @param tokenInfoRequestExecutor
     *            {@link TokenInfoRequestExecutor} to be wrapped
     * @param cause
     *            {@link OAuth2Exception} that will be thrown instead of the
     *            original
     * @return
     */
    public static TokenInfoRequestExecutor wrap(TokenInfoRequestExecutor tokenInfoRequestExecutor,
            OAuth2Exception cause) {
        return wrap(tokenInfoRequestExecutor, rethrowExceptionsAsList(OAuth2Exception.class),
                cause);
    }

    /**
     * Wraps the provided {@link TokenInfoRequestExecutor} to re-throw any
     * {@link RuntimeException} thrown as {@link InvalidTokenException} except
     * the class of the {@link RuntimeException}-subclass was registered in the
     * list of 'rethrowExceptions'.
     * 
     * @param tokenInfoRequestExecutor
     *            {@link TokenInfoRequestExecutor} to be wrapped
     * @param rethrowExceptions
     *            list of {@link RuntimeException}-subclasses to be re-thrown as
     *            is
     * @return
     */
    public static TokenInfoRequestExecutor wrap(TokenInfoRequestExecutor tokenInfoRequestExecutor,
            List<Class<? extends RuntimeException>> rethrowExceptions) {
        return wrap(tokenInfoRequestExecutor, rethrowExceptions, new InvalidTokenException(ACCESS_TOKEN_NOT_VALID));
    }

    /**
     * Wraps the provided {@link TokenInfoRequestExecutor} to re-throw any
     * {@link RuntimeException} thrown as the provided {@link OAuth2Exception}
     * -instance expect the class of the {@link RuntimeException}-subclass was
     * registered in the list of 'rethrowExceptions'.
     * 
     * @param tokenInfoRequestExecutor
     *            {@link TokenInfoRequestExecutor} to be wrapped
     * @param rethrowExceptions
     *            list of {@link RuntimeException}-subclasses to be re-thrown as
     *            is
     * @param cause
     *            {@link OAuth2Exception} that will be thrown instead of the
     *            original
     * @return
     */
    public static TokenInfoRequestExecutor wrap(TokenInfoRequestExecutor tokenInfoRequestExecutor,
            List<Class<? extends RuntimeException>> rethrowExceptions, OAuth2Exception cause) {
        return new RequestExecutorWrapper(tokenInfoRequestExecutor, rethrowExceptions, cause);
    }

    /**
     * Factory-method for List of {@link RuntimeException}-subclasses that will
     * be re-thrown as they are.
     * 
     * @param classes
     * @return
     */
    public static List<Class<? extends RuntimeException>> rethrowExceptionsAsList(
            Class<? extends RuntimeException>... classes) {

        List<Class<? extends RuntimeException>> rethrowExceptions = new LinkedList<>();
        rethrowExceptions.addAll(Arrays.asList(classes));
        return rethrowExceptions;
    }

    private static class RequestExecutorWrapper implements TokenInfoRequestExecutor {

        private final Logger log = LoggerFactory.getLogger(RequestExecutorWrapper.class);

        private final TokenInfoRequestExecutor delegate;
        private final OAuth2Exception cause;
        private final List<Class<? extends RuntimeException>> rethrowExceptions;

        private RequestExecutorWrapper(TokenInfoRequestExecutor delegate,
                List<Class<? extends RuntimeException>> rethrowExceptions, OAuth2Exception cause) {

            Assert.notNull(delegate, "'delegate' should never be null");
            Assert.notNull(cause, "'cause' should never be null");
            Assert.notNull(rethrowExceptions, "'rethrowExceptions' should never be null");

            this.delegate = delegate;
            this.cause = cause;
            this.rethrowExceptions = rethrowExceptions;
        }

        @Override
        public Map<String, Object> getMap(String accessToken) {
            try {
                return delegate.getMap(accessToken);
            } catch (RuntimeException e) {
                if (rethrowExceptions.contains(e.getClass())) {
                    throw e;
                } else {
                    log.error(e.getMessage(), e);
                    throw cause;
                }
            }
        }
    }
}
