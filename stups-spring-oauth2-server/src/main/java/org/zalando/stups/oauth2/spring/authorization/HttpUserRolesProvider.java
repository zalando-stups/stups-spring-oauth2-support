package org.zalando.stups.oauth2.spring.authorization;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import static org.springframework.security.oauth2.common.OAuth2AccessToken.BEARER_TYPE;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.ParameterizedTypeReference;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import org.springframework.util.Assert;

import org.springframework.web.client.RestTemplate;

import org.zalando.stups.oauth2.spring.server.TokenResponseErrorHandler;

public class HttpUserRolesProvider implements UserRolesProvider {

    private final String roleInfoUri;

    private final String rolePrefix;

    private static final String ROLE_SEPARATOR = "_";

    RestTemplate restTemplate = buildRestTemplate();

    public HttpUserRolesProvider(final String roleInfoUri, final String rolePrefix) {
        Assert.hasText(roleInfoUri, "roleInfoUri should never be null or empty");

        this.roleInfoUri = roleInfoUri;
        this.rolePrefix = rolePrefix;
    }

    public HttpUserRolesProvider(final String roleInfoUri) {
        this(roleInfoUri, null);
    }

    @Override
    public List<String> getUserRoles(final String uid, final String accessToken) {

        Assert.hasText(uid, "uid should never be null or empty");
        Assert.hasText(uid, "accessToken should never be null or empty");

        final HttpEntity<String> entity = getHttpEntity(accessToken);
        final ParameterizedTypeReference<List<Role>> responseType = new ParameterizedTypeReference<List<Role>>() { };

        final List<Role> roleList = restTemplate.exchange(roleInfoUri, HttpMethod.GET, entity, responseType, uid)
                                                .getBody();
        final List<String> rolesList = new ArrayList<>();
        for (final Role role : roleList) {
            if (rolePrefix != null) {
                rolesList.add(rolePrefix + ROLE_SEPARATOR + role.getName());
            } else {
                rolesList.add(role.getName());
            }
        }

        return rolesList;
    }

    private HttpEntity<String> getHttpEntity(final String accessToken) {
        final HttpHeaders headers = new HttpHeaders();
        headers.set(AUTHORIZATION, BEARER_TYPE + " " + accessToken);
        return new HttpEntity<>(headers);
    }

    private RestTemplate buildRestTemplate() {
        final RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(TokenResponseErrorHandler.getDefault());
        return restTemplate;
    }

}
