package org.zalando.stups.oauth2.spring.authorization;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.is;

import static org.mockito.Matchers.eq;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.junit.runner.RunWith;

import org.mockito.Matchers;
import org.mockito.Mock;

import org.mockito.runners.MockitoJUnitRunner;

import org.springframework.core.ParameterizedTypeReference;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.client.RestTemplate;

import junit.framework.TestCase;

@RunWith(MockitoJUnitRunner.class)
public class HttpUserRolesProviderTest extends TestCase {

    private static final String USER_UID = "user_uid";

    private static final String ROLE_PREFIX = "ROLE";

    private static final String ROLE_INFO_URL = "ROLES_INFO_URL";

    @Mock
    private RestTemplate restTemplate;

    private HttpUserRolesProvider httpUserRolesProvider;

    private List<Role> usersRoles;

    private ResponseEntity<List<Role>> responseEntity;

    @Before
    public void setUp() throws Exception {
        httpUserRolesProvider = new HttpUserRolesProvider(ROLE_INFO_URL, ROLE_PREFIX);
        httpUserRolesProvider.restTemplate = restTemplate;
        usersRoles = new RoleListBuilder().append("dn1", "name1").append("dn2", "name2").append("dn3", "name3").build();
        responseEntity = new ResponseEntity<>(usersRoles, HttpStatus.OK);
    }

    @Test
    public void testGetUserRoles() throws Exception {
        when(restTemplate.exchange(eq("ROLES_INFO_URL"), eq(HttpMethod.GET), Matchers.<HttpEntity<String>>any(),
                Matchers.<ParameterizedTypeReference<List<Role>>>any(), eq(USER_UID))).thenReturn(responseEntity);

        List<String> userRolesResponse = httpUserRolesProvider.getUserRoles(USER_UID, "access_token");

        for (String roleName : userRolesResponse) {
            assertThat(hasUserRole(roleName), is(true));
        }
    }

    @Test
    public void testGetUserRolesWithoutPrefix() throws Exception {
        httpUserRolesProvider = new HttpUserRolesProvider(ROLE_INFO_URL);
        httpUserRolesProvider.restTemplate = restTemplate;

        when(restTemplate.exchange(eq("ROLES_INFO_URL"), eq(HttpMethod.GET), Matchers.<HttpEntity<String>>any(),
                Matchers.<ParameterizedTypeReference<List<Role>>>any(), eq(USER_UID))).thenReturn(responseEntity);

        List<String> userRolesResponse = httpUserRolesProvider.getUserRoles(USER_UID, "access_token");

        for (String roleName : userRolesResponse) {
            assertThat(hasUserRoleWithoutPrefix(roleName), is(true));
        }
    }

    private boolean hasUserRole(final String role) {
        for (Role group : usersRoles) {
            if (role.equals(ROLE_PREFIX + "_" + group.getName())) {
                return true;
            }
        }

        return false;
    }

    private boolean hasUserRoleWithoutPrefix(final String role) {
        for (Role group : usersRoles) {
            if (role.equals(group.getName())) {
                return true;
            }
        }

        return false;
    }

    public static class RoleListBuilder {

        private List<Role> roles = new ArrayList<>();

        public RoleListBuilder append(final String dn, final String name) {
            Role role = new Role();
            role.setDn(dn);
            role.setName(name);
            roles.add(role);
            return this;
        }

        public List<Role> build() {
            return roles;
        }

    }

}
