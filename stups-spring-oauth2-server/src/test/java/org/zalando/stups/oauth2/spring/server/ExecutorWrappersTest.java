/**
 * Copyright (C) 2016 Zalando SE (http://tech.zalando.com)
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

import java.util.HashMap;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.common.exceptions.UnsupportedResponseTypeException;

public class ExecutorWrappersTest {

    private final String TOKEN = "234566778";
    TokenInfoRequestExecutor executor;

    @Before
    public void setUp() {
        executor = Mockito.mock(TokenInfoRequestExecutor.class);
    }

    @Test
    public void testDefault() {
        TokenInfoRequestExecutor wrapped = ExecutorWrappers.wrap(executor);
        Mockito.when(executor.getMap(Mockito.eq(TOKEN))).thenThrow(new RuntimeException("TEST_RUNTIME_EXCEPTION"));
        try {
            wrapped.getMap(TOKEN);
            Assertions.fail("Should throw an exception");
        } catch (InvalidTokenException e) {
            //
        } catch (RuntimeException e) {
            Assertions.fail("InvalidTokenException expected");
        }
    }

    @Test
    public void testDefaultWithOAuth2Exception() {
        TokenInfoRequestExecutor wrapped = ExecutorWrappers.wrap(executor);
        Mockito.when(executor.getMap(Mockito.eq(TOKEN))).thenThrow(new OAuth2Exception("TEST_RUNTIME_EXCEPTION"));
        try {
            wrapped.getMap(TOKEN);
            Assertions.fail("Should throw an exception");
        } catch (InvalidTokenException e) {
            Assertions.fail("InvalidTokenException expected");
        } catch (OAuth2Exception e) {
            // wanted
        } catch (RuntimeException e) {
            Assertions.fail("InvalidTokenException expected");
        }
    }

    @Test
    public void testDefaultWithCustomOAuth2Exception() {

        TokenInfoRequestExecutor wrapped = ExecutorWrappers.wrap(executor,
                new UnsupportedResponseTypeException("TEST_CAUSE"));

        Mockito.when(executor.getMap(Mockito.eq(TOKEN))).thenThrow(new RuntimeException("TEST_RUNTIME_EXCEPTION"));

        try {
            wrapped.getMap(TOKEN);
            Assertions.fail("Should throw an exception");
        } catch (InvalidTokenException e) {
            Assertions.fail("InvalidTokenException expected");
        } catch (OAuth2Exception e) {
            Assertions.assertThat(e.getMessage()).isEqualTo("TEST_CAUSE");
        } catch (RuntimeException e) {
            Assertions.fail("InvalidTokenException expected");
        }
    }

    @Test
    public void testDefaultWithSuccessfullExecution() {

        TokenInfoRequestExecutor wrapped = ExecutorWrappers.wrap(executor,
                new UnsupportedResponseTypeException("TEST_CAUSE"));

        Mockito.when(executor.getMap(Mockito.eq(TOKEN))).thenReturn(new HashMap<String, Object>());
        wrapped.getMap(TOKEN);
    }

}
