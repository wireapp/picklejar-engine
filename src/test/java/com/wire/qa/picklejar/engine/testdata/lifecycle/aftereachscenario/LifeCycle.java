package com.wire.qa.picklejar.engine.testdata.lifecycle.aftereachscenario;

import com.wire.qa.picklejar.engine.TestContext;
import com.wire.qa.picklejar.engine.annotations.AfterEachScenario;
import com.wire.qa.picklejar.engine.gherkin.model.Scenario;

public class LifeCycle {

    @AfterEachScenario
    public TestContext afterScenario(TestContext context, Scenario scenario) {
        return context;
    }
}
