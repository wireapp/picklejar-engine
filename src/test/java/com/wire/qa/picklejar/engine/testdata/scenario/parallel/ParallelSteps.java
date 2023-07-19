package com.wire.qa.picklejar.engine.testdata.scenario.parallel;

import io.cucumber.java.en.Given;

public class ParallelSteps {

    public ParallelSteps() {

    }

    @Given("Simple step")
    public void simpleStep() throws InterruptedException {
        Thread.sleep(100);
    }

}
