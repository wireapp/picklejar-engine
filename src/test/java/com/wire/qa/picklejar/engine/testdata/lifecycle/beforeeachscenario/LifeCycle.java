package com.wire.qa.picklejar.engine.testdata.lifecycle.beforeeachscenario;

import com.wire.qa.picklejar.engine.TestContext;
import com.wire.qa.picklejar.engine.annotations.BeforeEachScenario;
import com.wire.qa.picklejar.engine.gherkin.model.Scenario;

public class LifeCycle {

    @BeforeEachScenario
    public TestContext beforeScenario(Scenario scenario) {
        return new MyTestContext();
    }

}
