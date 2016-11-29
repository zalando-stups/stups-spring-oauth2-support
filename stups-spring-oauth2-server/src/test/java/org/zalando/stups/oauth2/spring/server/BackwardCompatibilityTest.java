package org.zalando.stups.oauth2.spring.server;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.Map;

import org.junit.Test;

public class BackwardCompatibilityTest {

    /*
     * This test checks the backwards compatibility for the
     * org.zalando.stups.oauth2.spring.server.DefaultAuthenticationExtractor
     * 
     * this test is for version 1.0.x
     */
    @Test
    public void checkingAbstractAuthenticationExtractor() throws NoSuchMethodException, SecurityException {
        Method methodSuperClazz = null;
        Class<?> cls = DefaultAuthenticationExtractor.class;
        do {
            try {
                methodSuperClazz = cls.getMethod("extractAuthentication", Map.class, String.class);
            } catch (NoSuchMethodException | SecurityException e) {

            }
            cls = cls.getSuperclass();
        } while (cls != null && methodSuperClazz == null);

        assertThat(methodSuperClazz).isNotNull();
    }
}
