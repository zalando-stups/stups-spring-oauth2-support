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

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Maybe this works.
 *
 * @author jbellmann
 */
@Controller
public class TokeninfoEndpoint {

    private final Logger logger = LoggerFactory.getLogger(TokeninfoEndpoint.class);

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/tokeninfo", params = { "access_token" })
    @ResponseBody
    public Map<String, Object> fakeTheResponse(@RequestParam("access_token") final String accessTokenParameter) {

        throw new RuntimeException("SHOULD NOT BE CALLED ANYMORE");
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/tokeninfo", params = { "!access_token" })
    @ResponseBody
    public ResponseEntity<Map<String, Object>> fakeTheResponseWithoutParam(WebRequest webRequest) {
        logger.warn("------- FAKE_TOKENINFO -------");
        String accessTokenParameter = webRequest.getHeader(HttpHeaders.AUTHORIZATION).substring("Bearer ".length());
        logger.warn("access_token : {}", accessTokenParameter);

        Map<String, Object> result = null;
        if (accessTokenParameter.contains("error")) {
            result = buildErrorResult();
        } else if (accessTokenParameter.contains("no-uid")) {
            result = buildAccessToken(accessTokenParameter);
            result.remove("uid");
            ((List<String>) result.get("scope")).remove("uid");
        } else if (accessTokenParameter.contains("empty-uid")) {
            result = buildAccessToken(accessTokenParameter);
            result.put("uid", "");
        } else if (accessTokenParameter.contains("no-scope")) {
            result = buildAccessToken(accessTokenParameter);
            result.remove("scope");
        } else if (accessTokenParameter.contains("lax")) {
            result = buildAccessToken(accessTokenParameter);
            result.remove("testscope");
            result.remove("simpleScope");
        } else if (accessTokenParameter.contains("lax-with-false")) {
            result = buildAccessToken(accessTokenParameter);
            result.put("testscope", Boolean.FALSE);
            result.put("simpleScope", Boolean.FALSE);
        } else if (accessTokenParameter.contains("invalidRealm")) {
            result = buildAccessToken(accessTokenParameter);
            result.put("realm", "nonexistent-realm");
        } else if (accessTokenParameter.contains("noRealm")) {
            result = buildAccessToken(accessTokenParameter);
            result.remove("realm");
        } else if (accessTokenParameter.contains("401")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(buildErrorResult("UNAUTHORIZED BY TOKENINFO"));
        } else if (accessTokenParameter.contains("403")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(buildErrorResult("FORBIDDEN BY TOKENINFO"));
        } else if (accessTokenParameter.contains("400")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(buildErrorResult("BAD_REQUEST BY TOKENINFO"));
        } else {
            result = buildAccessToken(accessTokenParameter);
        }

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    protected Map<String, Object> buildErrorResult() {
        return buildErrorResult("ErrorMessage");
    }

    protected Map<String, Object> buildErrorResult(String message) {
        Map<String, Object> result = Maps.newHashMap();
        result.put("error", message);
        return result;
    }

    protected Map<String, Object> buildAccessToken(final String accessTokenParameter) {
        Map<String, Object> result = Maps.newHashMap();
        result.put("uid", "klaus.tester");
        result.put("access_token", accessTokenParameter);
        result.put("token_type", "Bearer");
        result.put("expires_in", 5000);
        result.put("scope", Lists.newArrayList("uid", "simpleScope", "extrascope", "testscope"));
        result.put("testscope", Boolean.TRUE);
        result.put("simpleScope", Boolean.FALSE);
        result.put("realm", "/customrealm");

        return result;
    }
}
