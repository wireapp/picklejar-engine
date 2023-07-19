package com.wire.qa.picklejar.engine.gherkin.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"arguments", "location"})
public class Match implements Serializable {

    @JsonProperty("arguments")
    private List<Argument> arguments = new ArrayList<>();

    @JsonProperty("location")
    private String location;

    public Match() {
        this.arguments.add(new Argument());
        this.location = "location"+Math.random();
    }

    @Override
    public String toString() {
        return "Match{" + "arguments=" + arguments + ", location=" + location + '}';
    }

}
