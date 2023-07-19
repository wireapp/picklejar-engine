package com.wire.qa.picklejar.engine.gherkin.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"val", "offset"})
public class Argument implements Serializable {

    @JsonProperty("val")
    private String val;

    @JsonProperty("offset")
    private Long offset;

    public Argument() {
    }

    @Override
    public String toString() {
        return "Argument{" + "val=" + val + ", offset=" + offset + '}';
    }
}
