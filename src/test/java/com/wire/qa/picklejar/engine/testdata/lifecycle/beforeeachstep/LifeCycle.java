package com.wire.qa.picklejar.engine.testdata.lifecycle.beforeeachstep;

import com.wire.qa.picklejar.engine.TestContext;
import com.wire.qa.picklejar.engine.annotations.BeforeEachScenario;
import com.wire.qa.picklejar.engine.annotations.BeforeEachStep;
import com.wire.qa.picklejar.engine.gherkin.model.Scenario;
import com.wire.qa.picklejar.engine.gherkin.model.Step;

public class LifeCycle {

    public class MyTestContext extends TestContext {

    }

    public static int counter = 0;

    @BeforeEachScenario
    public TestContext beforeScenario(Scenario scenario) {
        return new MyTestContext();
    }

    @BeforeEachStep
    public TestContext beforeStep(TestContext context, Scenario scenario, Step step) {
        counter++;
        return context;
    }
}