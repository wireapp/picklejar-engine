package com.wire.qa.picklejar.engine.gherkin.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"line", "name"})
public class Tag implements Serializable {

    @JsonProperty("line")
    private long line;
    @JsonProperty("name")
    private String name;

    public Tag(String name) {
        this.line = 1;
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    @Override
    public String toString() {
        return "Tag{" + "line=" + line + ", name=" + name + '}';
    }

}
