package org.zalando.stups.oauth2.spring.server;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.zalando.stups.oauth2.spring.server.DefaultTokenInfoRequestExecutor.buildRestTemplate;

import java.util.EnumSet;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestTemplate;

/**
 * #30, NPE while tokenInfoResponse (Map<String,Object>) is null.
 * 
 * @author jbellmann
 *
 */
public class DefaultTokenInfoRequestExecutorTest {

    private RestTemplate restOperations;
    private MockRestServiceServer mockServer;

    @Before
    public void setUp() {

        restOperations = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restOperations);

        mockServer.expect(MockRestRequestMatchers.requestTo("http://example.com/tokenInfo"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withSuccess("", MediaType.APPLICATION_JSON));
    }

    @Test
    public void nullResultOnEmptyBody() {

        DefaultTokenInfoRequestExecutor executor = new DefaultTokenInfoRequestExecutor("http://example.com/tokenInfo",
                restOperations);

        Map<String, Object> result = executor.getMap("1234567890");
        Assertions.assertThat(result).isNull();

        mockServer.verify();
    }

    @Test(expected = InvalidTokenException.class)
    public void invalidTokenExceptionWhenEmptyBody(){

        DefaultTokenInfoRequestExecutor executor = new DefaultTokenInfoRequestExecutor("http://example.com/tokenInfo",
                restOperations);
        TokenInfoResourceServerTokenServices service = new TokenInfoResourceServerTokenServices("test", new DefaultAuthenticationExtractor(), executor);
        
        service.loadAuthentication("7364532");
    }

    @Test
    public void createRestTemplateWithEnumSet() {
        RestTemplate restTemplate = buildRestTemplate(EnumSet.of(BAD_REQUEST, UNAUTHORIZED, FORBIDDEN));
        Assertions.assertThat(restTemplate).isNotNull();
    }

}
