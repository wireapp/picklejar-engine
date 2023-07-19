package com.wire.qa.picklejar.engine.testdata.scenario.resourcelock;

import io.cucumber.java.en.Given;

public class ResourceLockSteps {

    public ResourceLockSteps() {

    }

    @Given("Simple step")
    public void simpleStep() throws InterruptedException {
        Thread.sleep(100);
    }

}
