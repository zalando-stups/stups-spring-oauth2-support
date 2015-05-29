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
package some.test.controller;

/**
 * Copyright 2015 Zalando SE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Maybe this works.
 *
 * @author  jbellmann
 */
@Controller
public class TokeninfoEndpoint {

    private final Logger logger = LoggerFactory.getLogger(TokeninfoEndpoint.class);

    @RequestMapping(value = "/tokeninfo")
    @ResponseBody
    public Map<String, Object> fakeTheResponse(@RequestHeader("Authorization") final String authorizationHeader) {
        logger.warn("------- FAKE_TOKENINFO -------");
        logger.warn("Authorization-header : {}", authorizationHeader);

        String accessTokenFromHeader = authorizationHeader.replace("Bearer ", "");
        Map<String, Object> result = null;
        if (authorizationHeader.contains("error")) {
            result = buildErrorResult();
        } else if (authorizationHeader.contains("no-uid")) {
            result = buildAccessToken(accessTokenFromHeader);
            result.remove("uid");
        } else if (authorizationHeader.contains("empty-uid")) {
            result = buildAccessToken(accessTokenFromHeader);
            result.put("uid", "");
        } else if (authorizationHeader.contains("no-scope")) {
            result = buildAccessToken(accessTokenFromHeader);
            result.remove("scope");
        } else {
            result = buildAccessToken(accessTokenFromHeader);
        }

        return result;
    }

    protected Map<String, Object> buildErrorResult() {
        Map<String, Object> result = Maps.newHashMap();
        result.put("error", "ErrorMessage");

        return result;
    }

    protected Map<String, Object> buildAccessToken(final String accessTokenFromHeader) {
        Map<String, Object> result = Maps.newHashMap();
        result.put("uid", "klaus.tester");
        result.put("access_token", accessTokenFromHeader);
        result.put("token_type", "Bearer");
        result.put("expires_in", 5000);
        result.put("scope", Lists.newArrayList("simpleScope", "extrascope", "testscope"));
        result.put("testscope", Boolean.TRUE);
        result.put("simpleScope", Boolean.FALSE);

        return result;
    }
}
