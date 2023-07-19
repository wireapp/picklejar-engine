package com.wire.qa.picklejar.engine.testdata.lifecycle.aftereachstep;

import com.wire.qa.picklejar.engine.TestContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

public class Steps {

    public Steps(LifeCycle.MyTestContext context) {

    }

    @Given("Simple step")
    public void step() {

    }

    @Then("Simple step with (.*) placeholder")
    public void step(String placeholder) {

    }

}
