package com.wire.qa.picklejar.engine.testdata.discovery.castplaceholders;

import io.cucumber.java.en.Given;

public class CastPlaceholdersSteps {

    public CastPlaceholdersSteps() {

    }

    @Given("^Step with (.*) as string parameter$")
    public void stringParameter(String parameter) {

    }

    @Given("^Step with (\\d+) as int parameter$")
    public void intParameter(int parameter) {

    }

    @Given("^Step with (\\d) as single int parameter$")
    public void intSingleParameter(int parameter) {

    }

    @Given("^Step with (\\d+) as long parameter$")
    public void longParameter(long parameter) {

    }

    @Given("^Step with ([-+]?[0-9]*\\.?[0-9]+) as float parameter$")
    public void floatParameter(float parameter) {

    }

    @Given("^Step with ([-+]?[0-9]*\\.?[0-9]+) as double parameter$")
    public void doubleParameter(double parameter) {

    }

    @Given("^Step with (.*) as boolean parameter$")
    public void booleanParameter(boolean parameter) {

    }
}
