package org.zalando.stups.oauth2.spring.authorization;

import java.util.ArrayList;
import java.util.List;

/**
 * Returns an empty list of 'ROLE's.
 * 
 * @author jbellmann
 *
 */
public class NoRolesUserRolesProvider implements UserRolesProvider {

    @Override
    public List<String> getUserRoles(String uid, String realm, String accessToken) {
        return new ArrayList<String>(0);
    }

}
