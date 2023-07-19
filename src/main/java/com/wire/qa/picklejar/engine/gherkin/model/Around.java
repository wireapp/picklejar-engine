package com.wire.qa.picklejar.engine.gherkin.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"result", "match"})
public class Around implements Serializable {

    @JsonProperty("result")
    private Result result;

    @JsonProperty("match")
    private Match match;

    public Around() {
        this.result = new Result(1L, "passed", null);
        this.match = new Match();
    }

    public Around(Result result) {
        this.result = result;
        this.match = new Match();
    }

    @Override
    public String toString() {
        return "Around{" + "result=" + result + ", match=" + match + '}';
    }
}
