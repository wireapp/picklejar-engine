package com.wire.qa.picklejar.engine.gherkin.model;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CucumberReport extends ArrayList<Feature> implements Serializable {

    static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String toString() {
        try {
            return MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException ex) {
            return super.toString();
        }
    }

    public void writeValue(File resultFile) throws IOException {
        MAPPER.writeValue(resultFile, this);
    }
}
