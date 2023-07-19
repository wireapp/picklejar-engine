package com.wire.qa.picklejar.engine.testdata.lifecycle.classvariable;

import io.cucumber.java.en.Given;

import static org.assertj.core.api.Assertions.assertThat;

public class ClassVariableSteps {

    private static String classVariable = null;

    public ClassVariableSteps() {

    }

    @Given("Step that sets class variable")
    public void setClassVariable() {
        classVariable = "newvalue";
    }

    @Given("Step that reads class variable")
    public void readClassVariable() {
        assertThat(classVariable).isEqualTo("newvalue");
    }
}
