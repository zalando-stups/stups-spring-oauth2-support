package org.zalando.stups.oauth2.spring.server;

import java.util.Map;

/**
 * To provide multiple implementations to request 'tokeninfo' from the endpoint.
 * 
 * @author jbellmann
 *
 */
public interface TokenInfoRequestExecutor {

    Map<String, Object> getMap(final String accessToken);

}
