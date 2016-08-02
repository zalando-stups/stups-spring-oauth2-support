package org.zalando.stups.oauth2.spring.authorization;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import static org.springframework.security.oauth2.common.OAuth2AccessToken.BEARER_TYPE;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.ParameterizedTypeReference;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import org.springframework.web.client.RestTemplate;

public class HttpUserRolesProvider implements UserRolesProvider {

    private final String roleInfoUri;

    private final String rolePrefix;

    private static final String ROLE_SEPARATOR = "_";

    RestTemplate restTemplate = new RestTemplate();

    public HttpUserRolesProvider(final String roleInfoUri, final String rolePrefix) {
        this.roleInfoUri = roleInfoUri;
        this.rolePrefix = rolePrefix;
    }

    @Override
    public List<String> getUserRoles(final String uid, final String accessToken) {
        HttpEntity<String> entity = getHttpEntity(accessToken);
        ParameterizedTypeReference<List<Role>> responseType = new ParameterizedTypeReference<List<Role>>() { };

        List<Role> roleList = restTemplate.exchange(roleInfoUri, HttpMethod.GET, entity, responseType, uid).getBody();
        List<String> rolesList = new ArrayList<>();
        for (Role role : roleList) {
            rolesList.add(rolePrefix + ROLE_SEPARATOR + role.getName());
        }

        return rolesList;
    }

    private HttpEntity<String> getHttpEntity(final String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTHORIZATION, BEARER_TYPE + " " + accessToken);
        return new HttpEntity<>(headers);
    }
}
