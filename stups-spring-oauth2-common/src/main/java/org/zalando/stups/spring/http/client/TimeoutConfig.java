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
package org.zalando.stups.spring.http.client;

import org.springframework.http.client.SimpleClientHttpRequestFactory;

/**
 * To create a {@link TimeoutConfig} use {@link TimeoutConfig.Builder}.
 * 
 * @author jbellmann
 *
 */
public class TimeoutConfig {

    private int readTimeout;

    private int connectTimeout;

    private int connectionRequestTimeout;

    private TimeoutConfig(int readTimeout, int connectTimeout, int connectionRequestTimeout) {
        this.readTimeout = readTimeout;
        this.connectTimeout = connectTimeout;
        this.connectionRequestTimeout = connectionRequestTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * Not supported on {@link SimpleClientHttpRequestFactory}.
     */
    public int getConnectionRequestTimeout() {
        return connectionRequestTimeout;
    }

    public static TimeoutConfig withDefaults() {
        return new Builder().build();
    }

    /**
     * Builder to create a {@link TimeoutConfig}.<br/>
     * - 'readTimeout'/'socketTimeout' defaults to 2000 ms.<br/>
     * - 'connectTimeout' defaults to 1000 ms.<br/>
     * - 'connectionRequestTimeout' defaults to 500 ms.
     * 
     *
     */
    public static class Builder {
        private int readTimeout = 2000;
        private int connectTimeout = 1000;
        private int connectionRequestTimeout = 500;

        public Builder withReadTimeout(int readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        public Builder withConnectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder withConnectionRequestTimeout(int connectionRequestTimeout) {
            this.connectionRequestTimeout = connectionRequestTimeout;
            return this;
        }

        public TimeoutConfig build() {
            return new TimeoutConfig(readTimeout, connectTimeout, connectionRequestTimeout);
        }
    }
}
