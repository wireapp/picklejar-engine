package com.wire.qa.picklejar.engine.testdata.scenario.twoexamples;

import io.cucumber.java.en.Given;

import static org.assertj.core.api.Assertions.assertThat;

public class TwoExamplesSteps {

    public TwoExamplesSteps() {

    }

    @Given("Step which uses placeholder (.*) from Examples")
    public void stepWithPlaceholder(String placeholder) {
        assertThat(placeholder).startsWith("Value");
    }

}
