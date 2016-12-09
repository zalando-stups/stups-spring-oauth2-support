package org.zalando.stups.oauth2.spring.authorization;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.security.oauth2.common.OAuth2AccessToken.BEARER_TYPE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.core.ParameterizedTypeReference;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;

import org.springframework.web.client.RestTemplate;

import org.zalando.stups.oauth2.spring.server.TokenResponseErrorHandler;
import org.zalando.stups.spring.http.client.ClientHttpRequestFactorySelector;

public class HttpUserRolesProvider implements UserRolesProvider {

    public static final ParameterizedTypeReference<List<Role>> ROLES = new ParameterizedTypeReference<List<Role>>() {
    };

    public static final String EMPLOYEES = "/employees";
    public static final String DEFAULT_ROLE = "ROLE_USER";

    private static final String ROLE_SEPARATOR = "_";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final String roleInfoUri;
    private final String rolePrefix;
    private final RestTemplate restTemplate;

    public HttpUserRolesProvider(final String roleInfoUri) {
        this(roleInfoUri, null);
    }

    public HttpUserRolesProvider(final String roleInfoUri, final String rolePrefix) {
        this(roleInfoUri, rolePrefix, null);
    }

    public HttpUserRolesProvider(final String roleInfoUri, final String rolePrefix, final RestTemplate restTemplate) {
        Assert.hasText(roleInfoUri, "roleInfoUri should never be null or empty");

        this.roleInfoUri = roleInfoUri;
        this.rolePrefix = rolePrefix;
        this.restTemplate = restTemplate == null ? buildRestTemplate() : restTemplate;
    }

    private static RestTemplate buildRestTemplate() {
        final RestTemplate restTemplate = new RestTemplate(ClientHttpRequestFactorySelector.getRequestFactory());
        restTemplate.setErrorHandler(TokenResponseErrorHandler.getDefault());
        return restTemplate;
    }

    @Override
    public List<String> getUserRoles(final String uid, final String realm, final String accessToken) {
        Assert.hasText(uid, "uid should never be null or empty");
        Assert.hasText(uid, "accessToken should never be null or empty");

        final List<String> rolesList = new ArrayList<>();

        if (EMPLOYEES.equals(realm)) {
            final HttpEntity<String> entity = getHttpEntity(accessToken);
            final ResponseEntity<List<Role>> response = restTemplate.exchange(roleInfoUri, GET, entity, ROLES, uid);

            final List<Role> roleList = response.getBody();

            if (roleList == null) {
                logger.warn("No roles can be extracted for uid ({}) !", uid);
                return Collections.emptyList();
            }

            for (final Role role : roleList) {
                if (rolePrefix != null) {
                    rolesList.add(rolePrefix + ROLE_SEPARATOR + role.getName());
                } else {
                    rolesList.add(role.getName());
                }
            }
        } else {
            rolesList.add(DEFAULT_ROLE);
        }

        return rolesList;
    }

    private HttpEntity<String> getHttpEntity(final String accessToken) {
        final HttpHeaders headers = new HttpHeaders();
        headers.set(AUTHORIZATION, BEARER_TYPE + " " + accessToken);
        return new HttpEntity<>(headers);
    }

}
