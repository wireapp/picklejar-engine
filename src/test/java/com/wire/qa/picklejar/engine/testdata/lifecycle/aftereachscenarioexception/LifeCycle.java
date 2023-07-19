package com.wire.qa.picklejar.engine.testdata.lifecycle.aftereachscenarioexception;

import com.wire.qa.picklejar.engine.TestContext;
import com.wire.qa.picklejar.engine.annotations.AfterEachScenario;
import com.wire.qa.picklejar.engine.annotations.BeforeEachScenario;
import com.wire.qa.picklejar.engine.gherkin.model.Scenario;

public class LifeCycle {

    public class MyTestContext extends TestContext {

    }

    @BeforeEachScenario
    public MyTestContext beforeScenario(Scenario scenario) {
        return new MyTestContext();
    }

    @AfterEachScenario
    public MyTestContext afterScenario(MyTestContext context, Scenario scenario) {
        throw new RuntimeException("Exception in @AfterEachScenario");
    }
}
