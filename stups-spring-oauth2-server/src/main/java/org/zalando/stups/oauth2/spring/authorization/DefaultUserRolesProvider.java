package org.zalando.stups.oauth2.spring.authorization;

import java.util.Arrays;
import java.util.List;

public class DefaultUserRolesProvider implements UserRolesProvider {

    @Override
    public List<String> getUserRoles(final String uid, final String realm, final String accessToken) {
        return Arrays.asList("ROLE_USER");
    }

}
