package org.zalando.stups.oauth2.spring.security.expression;

import java.io.IOException;
import java.nio.charset.Charset;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;

/**
 * 
 * @author jbellmann
 *
 */
public class ResourceSupport {

    private Class<?> clazz;

    public ResourceSupport(Class<?> clazz) {
        this.clazz = clazz;
    }

    protected Resource jsonResource(String filename) {
        return new ClassPathResource(filename + ".json", clazz);
    }

    protected String resourceToString(Resource resource) throws IOException {
        return StreamUtils.copyToString(resource.getInputStream(), Charset.defaultCharset());
    }

}
