package org.zalando.stups.oauth2.spring.authorization;

import java.util.List;

public interface UserRolesProvider {

    List<String> getUserRoles(String uid, String realm, String accessToken);

}
