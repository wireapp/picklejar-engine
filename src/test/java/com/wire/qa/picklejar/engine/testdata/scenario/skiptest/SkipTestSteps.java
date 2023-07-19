package com.wire.qa.picklejar.engine.testdata.scenario.skiptest;

import com.wire.qa.picklejar.engine.exception.SkipException;
import io.cucumber.java.en.Given;

public class SkipTestSteps {

    public SkipTestSteps() {

    }

    @Given("I skip the test on condition")
    public void skipStep() {
        throw new SkipException("Because the method just skips for testing");
    }
}
