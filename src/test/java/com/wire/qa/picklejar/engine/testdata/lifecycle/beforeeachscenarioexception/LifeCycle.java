package com.wire.qa.picklejar.engine.testdata.lifecycle.beforeeachscenarioexception;

import com.wire.qa.picklejar.engine.TestContext;
import com.wire.qa.picklejar.engine.annotations.BeforeEachScenario;
import com.wire.qa.picklejar.engine.gherkin.model.Scenario;

public class LifeCycle {

    @BeforeEachScenario
    public TestContext beforeScenario(Scenario scenario) {
        throw new RuntimeException("Exception in @BeforeEachScenario");
    }
}
