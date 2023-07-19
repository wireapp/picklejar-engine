package com.wire.qa.picklejar.engine.testdata.lifecycle.beforeeachscenario;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

public class Steps {

    public Steps(MyTestContext context) {

    }

    @Given("Simple step")
    public void step() {

    }

    @Then("Simple step with (.*) placeholder")
    public void step(String placeholder) {

    }

}
