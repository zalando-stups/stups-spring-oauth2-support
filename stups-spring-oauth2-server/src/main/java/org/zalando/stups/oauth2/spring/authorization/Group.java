package org.zalando.stups.oauth2.spring.authorization;

import org.codehaus.jackson.annotate.JsonProperty;

public class Group {

    @JsonProperty(value = "dn")
    private String dn;

    @JsonProperty(value = "name")
    private String name;

    public String getDn() {
        return dn;
    }

    public void setDn(final String dn) {
        this.dn = dn;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

}
