package org.zalando.stups.oauth2.spring.authorization;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HttpUserRolesProviderTest {

    private static final String ROLE_PREFIX = "ROLE";
    private static final String ROLE_INFO_URL = "ROLES_INFO_URL";
    private static final String USER_UID = "user_uid";
    private static final String EMPLOYEES = "/employees";

    private final RestTemplate restTemplate = mock(RestTemplate.class);

    private final List<Role> usersRoles = Arrays.asList(
            newRole("dn1", "name1"),
            newRole("dn2", "name2"),
            newRole("dn3", "name3")
    );

    private final ResponseEntity<List<Role>> responseEntity = new ResponseEntity<>(usersRoles, HttpStatus.OK);

    @Before
    public void defaultBehaviour() {
        when(restTemplate.exchange(eq("ROLES_INFO_URL"), eq(HttpMethod.GET), Matchers.<HttpEntity<String>>any(),
                Matchers.<ParameterizedTypeReference<List<Role>>>any(), eq(USER_UID))).thenReturn(responseEntity);
    }

    @Test
    public void testGetUserRoles() throws Exception {
        final HttpUserRolesProvider unit = new HttpUserRolesProvider(ROLE_INFO_URL, ROLE_PREFIX, restTemplate);
        final List<String> roles = unit.getUserRoles(USER_UID, EMPLOYEES, "access_token");

        assertThat(roles, hasSize(usersRoles.size()));

        for (final Role role : usersRoles) {
            assertThat(roles, hasItem(ROLE_PREFIX + "_" + role.getName()));
        }
    }

    @Test
    public void testGetUserRolesWithoutPrefix() throws Exception {
        final HttpUserRolesProvider unit = new HttpUserRolesProvider(ROLE_INFO_URL, null, restTemplate);
        final List<String> roles = unit.getUserRoles(USER_UID, EMPLOYEES, "access_token");

        assertThat(roles, hasSize(usersRoles.size()));

        for (final Role role : usersRoles) {
            assertThat(roles, hasItem(role.getName()));
        }
    }

    private static Role newRole(final String dn, final String name) {
        final Role role = new Role();
        role.setDn(dn);
        role.setName(name);
        return role;
    }

}
