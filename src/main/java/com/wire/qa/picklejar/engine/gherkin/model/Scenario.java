package com.wire.qa.picklejar.engine.gherkin.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"before", "line", "name", "description", "id", "after", "type", "keyword", "steps"})
public class Scenario implements Serializable {

    @JsonProperty("before")
    private List<Around> before;
    @JsonProperty("after")
    private List<Around> after;
    @JsonProperty("line")
    private long line;
    private Feature feature;
    @JsonProperty("name")
    private String name;
    @JsonProperty("description")
    private String description;
    @JsonProperty("id")
    private String id;
    @JsonProperty("keyword")
    private String keyword;
    @JsonProperty("type")
    private String type;
    @JsonProperty("steps")
    private List<Step> steps;
    @JsonProperty("tags")
    private List<Tag> tags;

    public Scenario(Feature feature, String name, String description, int exampleNum, String keyword, List<Step> steps,
                    List<Tag> tags) {
        this.feature = feature;
        this.line = 1;
        this.name = name.trim();
        this.id = (feature.getName().toLowerCase() + ";" + this.name.toLowerCase()).replaceAll("[^a-zA-Z0-9]", "-")+";;"+exampleNum;
        this.type = "scenario";
        this.keyword = keyword;
        this.steps = steps;
        this.tags = tags;
        this.description = description;
        this.before = Arrays.asList(new Around());
        this.after = Arrays.asList(new Around());
    }

    public List<Step> getSteps() {
        return steps;
    }

    public String getName() {
        return name;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setBefore(Around around) {
        this.before = Arrays.asList(around);
    }

    public void setAfter(Around around) {
        this.after = Arrays.asList(around);
    }

    @JsonIgnore
    public Feature getFeature() {
        return feature;
    }
    
    @Override
    public String toString() {
        return "Scenario{" + "before=" + before + ", after=" + after + ", line=" + line + ", name=" + name + ", description=" + description + ", id=" + id + ", keyword=" + keyword + ", type=" + type + ", steps=" + steps + ", tags=" + tags + '}';
    }

    public boolean hasTag(String tagName) {
        // Check for tags case-insensitive
        return getTags().stream().anyMatch(tag -> tag.getName().toLowerCase().contains(tagName.toLowerCase()));
    }

}
