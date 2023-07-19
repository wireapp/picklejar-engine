package com.wire.qa.picklejar.engine.gherkin.model;

import java.io.Serializable;
import java.util.Base64;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"data", "mime_type"})
public class Embeddings implements Serializable {

    @JsonProperty("data")
    private String data;

    @JsonProperty("mime_type")
    private String mime_type;

    public Embeddings(String data, String mime_type) {
        this.data = data;
        this.mime_type = mime_type;
    }

    public Embeddings(byte[] data, String mime_type) {
        Base64.Encoder encoder = Base64.getEncoder();
        this.data = encoder.encodeToString(data);
        this.mime_type = mime_type;
    }

    @Override
    public String toString() {
        return "Embeddings{" + "data=" + data + ", mime_type=" + mime_type + '}';
    }
}
