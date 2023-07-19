package com.wire.qa.picklejar.engine.gherkin.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"duration", "error_message", "status"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result implements Serializable {
    
    public static final String SKIPPED = "skipped";
    public static final String UNDEFINED = "undefined";
    public static final String PASSED = "passed";
    public static final String FAILED = "failed";

    @JsonProperty("duration")
    private Long duration;
    @JsonProperty("status")
    private String status;
    @JsonProperty("error_message")
    private String error_message;

    public Result(Long duration, String status, String error_message) {
        this.duration = duration;
        this.status = status;
        this.error_message = error_message;
    }

    public String getStatus() {
        return status;
    }

    public boolean isSkipped() {
        return SKIPPED.equals(status);
    }

    public Long getDuration() {
        return duration;
    }

    public String getErrorMessage() {
        return error_message;
    }
    
    @Override
    public String toString() {
        return "Result{status=" + status + ", duration=" + duration + ", error_message=...}";
    }
}
