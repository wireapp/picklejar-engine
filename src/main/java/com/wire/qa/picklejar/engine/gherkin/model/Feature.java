package com.wire.qa.picklejar.engine.gherkin.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"line", "elements", "name", "description", "id", "keyword", "uri"})
public class Feature implements Serializable {

    @JsonProperty("line")
    private long line;
    @JsonProperty("elements")
    private List<Scenario> scenarios = new ArrayList<>();
    @JsonProperty("name")
    private String name;
    @JsonProperty("description")
    private String description;
    @JsonProperty("id")
    private String id;
    @JsonProperty("keyword")
    private String keyword;
    @JsonProperty("uri")
    private String uri;

    public Feature(String name, List<Scenario> scenarios) {
        this.line = 1;
        this.scenarios = scenarios;
        this.name = name.trim();
        this.id = this.name.toLowerCase().replaceAll("[^a-zA-Z0-9]", "-");
        this.keyword = "Feature";
        this.description = "";
        this.uri = "/uri/"+this.name;
    }
    
    public Feature(String name) {
        this.line = 1;
        this.name = name.trim();
        this.id = this.name.toLowerCase();
        this.keyword = "Feature";
        this.description = "";
        this.uri = "uri/"+this.name+".feature";
    }

    @Override
    public String toString() {
        return "Feature{" + "line=" + line + ", scenarios=" + scenarios + ", name=" + name + ", description=" + description + ", id=" + id + ", keyword=" + keyword + ", uri=" + uri + '}';
    }

    public List<Scenario> getScenarios() {
        return scenarios;
    }

    public void setScenarios(List<Scenario> scenarios) {
        this.scenarios = scenarios;
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 47 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Feature other = (Feature) obj;
        return Objects.equals(this.name, other.name);
    }

}
