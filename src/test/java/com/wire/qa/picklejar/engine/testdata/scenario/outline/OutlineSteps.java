package com.wire.qa.picklejar.engine.testdata.scenario.outline;

import io.cucumber.java.en.Given;

import static org.assertj.core.api.Assertions.assertThat;

public class OutlineSteps {

    public OutlineSteps() {

    }

    @Given("Step which uses placeholder (.*) from Examples")
    public void stepWithPlaceholder(String placeholder) {
        assertThat(placeholder).startsWith("Value");
    }

}
