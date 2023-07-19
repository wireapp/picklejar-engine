package com.wire.qa.picklejar.engine.gherkin.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import static com.wire.qa.picklejar.engine.gherkin.model.Result.SKIPPED;

@JsonPropertyOrder({"result", "comments", "embeddings", "line", "name", "match", "matchedColumns", "keyword"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Step implements Serializable {

    @JsonProperty("line")
    private long line;
    @JsonProperty("name")
    private String name;
    @JsonProperty("keyword")
    private String keyword;
    @JsonProperty("result")
    private Result result;
    @JsonProperty("match")
    private Match match;
    @JsonProperty("embeddings")
    private List<Embeddings> embeddings;
    @JsonProperty("matchedColumns")
    private int[] matchedColumns;
    @JsonProperty("comments")
    private List<Comment> comments;

    public Step(String keyword, String name) {
        this.line = 1;
        this.name = name;
        this.keyword = keyword + " ";
        this.match = new Match();
        this.result = new Result(1L, SKIPPED, null);
        this.embeddings = new ArrayList<>();
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public Result getResult() {
        return result;
    }
    
    public String getName() {
        return name;
    }

    public void addEmbedding(Embeddings embedding) {
        embeddings.add(embedding);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Step other = (Step) obj;
        return Objects.equals(this.name, other.name);
    }
    
    
    
    @Override
    public String toString() {
        /*
        try {
            return MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException ex) {
            return super.toString();
        }
        */
        return super.toString();
    }

}
