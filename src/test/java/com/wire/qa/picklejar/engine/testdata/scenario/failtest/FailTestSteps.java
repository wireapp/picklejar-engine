package com.wire.qa.picklejar.engine.testdata.scenario.failtest;

import io.cucumber.java.en.Given;

import static org.assertj.core.api.Assertions.assertThat;

public class FailTestSteps {

    public FailTestSteps() {

    }

    @Given("I fail")
    public void skipStep() {
        assertThat(false).isTrue();
    }
}
