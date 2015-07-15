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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.stups.oauth2.spring.client.AccessTokenUtils;

import java.util.Optional;
import java.util.Set;

/**
 * The Resource we secured with OAuth2.
 *
 * @author jbellmann
 */
@RestController
public class SecuredResource {

    private final Logger logger = LoggerFactory.getLogger(SecuredResource.class);

    @RequestMapping("/secured/hello/{term}")
    public String hello(@PathVariable final String term) {
        Optional<String> accessToken = AccessTokenUtils.getAccessTokenFromSecurityContext();
        logger.info("SECURED-RESOURCE ACCESSED WITH TOKEN : {}", accessToken.get());
        return "hello " + term;
    }

    @RequestMapping("/secured/bye")
    public Set<String> bye(final OAuth2Authentication auth) {
        return auth.getOAuth2Request().getScope();
    }

}
