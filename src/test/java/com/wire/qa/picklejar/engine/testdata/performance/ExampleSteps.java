package com.wire.qa.picklejar.engine.testdata.performance;

import io.cucumber.java.en.Given;

public class ExampleSteps {

    public ExampleSteps() {

    }

    @Given("Step without parameters")
    public void step() {

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

    @Given("^Step with (.*) and (.*) and (.*) and (.*) different parameters$")
    public void booleanParameter(boolean parameter, double parameter2, long parameter3, String parameter4) {

    }
}
