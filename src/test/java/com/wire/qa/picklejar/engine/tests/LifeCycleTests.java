package com.wire.qa.picklejar.engine.tests;

import com.wire.qa.picklejar.engine.testdata.lifecycle.beforeeachscenario.LifeCycle;
import org.junit.jupiter.api.Test;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;

import static com.wire.qa.picklejar.engine.tests.ReportEntryConditions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;
import static org.junit.platform.testkit.engine.EventConditions.*;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.instanceOf;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.message;

public class LifeCycleTests {

    @Test
    public void classVariableUsage() {
        EngineExecutionResults results = EngineTestKit
                .engine("picklejar-engine")
                .enableImplicitConfigurationParameters(false)
                .configurationParameter("com.wire.qa.picklejar.steps.packages", "com.wire.qa.picklejar.engine.testdata.lifecycle.classvariable")
                .configurationParameter("com.wire.qa.picklejar.features.package", "com.wire.qa.picklejar.engine.testdata.lifecycle.classvariable")
                .selectors(selectPackage(""))
                .execute();

        results.testEvents().assertStatistics(stats -> stats.started(1).succeeded(1));
    }

    @Test
    public void beforeEachScenario() {
        EngineExecutionResults results = EngineTestKit
                .engine("picklejar-engine")
                .enableImplicitConfigurationParameters(false)
                .configurationParameter("com.wire.qa.picklejar.steps.packages", "com.wire.qa.picklejar.engine.testdata.lifecycle.beforeeachscenario")
                .configurationParameter("com.wire.qa.picklejar.features.package", "com.wire.qa.picklejar.engine.testdata.lifecycle.beforeeachscenario")
                .selectors(selectPackage(""))
                .execute();

        results.testEvents().assertStatistics(stats -> stats.started(3).succeeded(3));
        results.testEvents().reportingEntryPublished().assertEventsMatchLoosely(
                reportEntry(clasz(LifeCycle.class, "beforeScenario"),
                        scenario("Scenario of Feature2 0"), ReportEntryConditions.status("started")),
                reportEntry(clasz(LifeCycle.class, "beforeScenario"),
                        scenario("Scenario of Feature2 0"), ReportEntryConditions.status("finished")),
                reportEntry(step("Simple step with Placeholder placeholder"), ReportEntryConditions.status("started")),
                reportEntry(step("Simple step with Placeholder placeholder"), ReportEntryConditions.status("passed")),
                reportEntry(clasz(LifeCycle.class, "beforeScenario"),
                        scenario("Scenario of Feature2 1"), ReportEntryConditions.status("started")),
                reportEntry(clasz(LifeCycle.class, "beforeScenario"),
                        scenario("Scenario of Feature2 1"), ReportEntryConditions.status("finished")),
                reportEntry(step("Simple step with Placeholder placeholder"), ReportEntryConditions.status("started")),
                reportEntry(step("Simple step with Placeholder placeholder"), ReportEntryConditions.status("passed")),
                reportEntry(clasz(LifeCycle.class, "beforeScenario"),
                        scenario("Scenario of Feature1"), ReportEntryConditions.status("started")),
                reportEntry(clasz(LifeCycle.class, "beforeScenario"),
                        scenario("Scenario of Feature1"), ReportEntryConditions.status("finished")),
                reportEntry(step("Simple step"), ReportEntryConditions.status("started")),
                reportEntry(step("Simple step"), ReportEntryConditions.status("passed")),
                reportEntry(step("Simple step"), ReportEntryConditions.status("started")),
                reportEntry(step("Simple step"), ReportEntryConditions.status("passed"))
        );
    }

    @Test
    public void beforeEachScenarioException() {
        EngineExecutionResults results = EngineTestKit
                .engine("picklejar-engine")
                .enableImplicitConfigurationParameters(false)
                .configurationParameter("com.wire.qa.picklejar.steps.packages", "com.wire.qa.picklejar.engine.testdata.lifecycle.beforeeachscenarioexception")
                .configurationParameter("com.wire.qa.picklejar.features.package", "com.wire.qa.picklejar.engine.testdata.lifecycle.beforeeachscenarioexception")
                .selectors(selectPackage(""))
                .execute();

        results.testEvents().assertStatistics(stats -> stats.started(1).failed(1));
        results.testEvents().failed().assertEventsMatchExactly(
                event(finishedWithFailure(instanceOf(RuntimeException.class),
                        message("Exception in @BeforeEachScenario")))
        );
    }

    @Test
    public void afterEachScenario() {
        EngineExecutionResults results = EngineTestKit
                .engine("picklejar-engine")
                .enableImplicitConfigurationParameters(false)
                .configurationParameter("com.wire.qa.picklejar.steps.packages", "com.wire.qa.picklejar.engine.testdata.lifecycle.aftereachscenario")
                .configurationParameter("com.wire.qa.picklejar.features.package", "com.wire.qa.picklejar.engine.testdata.lifecycle.aftereachscenario")
                .selectors(selectPackage(""))
                .execute();

        results.testEvents().assertStatistics(stats -> stats.started(3).succeeded(3));
        results.testEvents().reportingEntryPublished().assertEventsMatchLoosely(
                reportEntry(step("Simple step with Placeholder placeholder"), ReportEntryConditions.status("started")),
                reportEntry(step("Simple step with Placeholder placeholder"), ReportEntryConditions.status("passed")),
                reportEntry(clasz(com.wire.qa.picklejar.engine.testdata.lifecycle.aftereachscenario.LifeCycle.class,
                        "afterScenario"),
                        scenario("Scenario of Feature2 0"), ReportEntryConditions.status("started")),
                reportEntry(step("Simple step with Placeholder placeholder"), ReportEntryConditions.status("started")),
                reportEntry(step("Simple step with Placeholder placeholder"), ReportEntryConditions.status("passed")),
                reportEntry(clasz(com.wire.qa.picklejar.engine.testdata.lifecycle.aftereachscenario.LifeCycle.class,
                        "afterScenario"),
                        scenario("Scenario of Feature2 1"), ReportEntryConditions.status("started")),
                //reportEntry(clasz(LifeCycle.class, "afterScenario"),
                //        scenario("Scenario of Feature2 0"), ReportEntryConditions.status("finished")),
                reportEntry(step("Simple step"), ReportEntryConditions.status("started")),
                reportEntry(step("Simple step"), ReportEntryConditions.status("passed")),
                reportEntry(step("Simple step"), ReportEntryConditions.status("started")),
                reportEntry(step("Simple step"), ReportEntryConditions.status("passed")),
                reportEntry(clasz(com.wire.qa.picklejar.engine.testdata.lifecycle.aftereachscenario.LifeCycle.class,
                        "afterScenario"),
                        scenario("Scenario of Feature1"), ReportEntryConditions.status("started"))
                //reportEntry(clasz(LifeCycle.class, "afterScenario"),
                //        scenario("Scenario of Feature1"), ReportEntryConditions.status("finished"))
        );
    }

    @Test
    public void afterEachScenarioException() {
        EngineExecutionResults results = EngineTestKit
                .engine("picklejar-engine")
                .enableImplicitConfigurationParameters(false)
                .configurationParameter("com.wire.qa.picklejar.steps.packages", "com.wire.qa.picklejar.engine.testdata.lifecycle.aftereachscenarioexception")
                .configurationParameter("com.wire.qa.picklejar.features.package", "com.wire.qa.picklejar.engine.testdata.lifecycle.aftereachscenarioexception")
                .selectors(selectPackage(""))
                .execute();

        results.testEvents().assertStatistics(stats -> stats.started(1).succeeded(0).failed(2));
        results.testEvents().failed().assertEventsMatchExactly(
                event(finishedWithFailure(instanceOf(RuntimeException.class),
                        message("Exception in @AfterEachScenario"))),
                event(finishedWithFailure(instanceOf(RuntimeException.class),
                        message("java.lang.RuntimeException: Exception in @AfterEachScenario")))
        );
    }

    @Test
    public void beforeEachStep() {
        EngineExecutionResults results = EngineTestKit
                .engine("picklejar-engine")
                .enableImplicitConfigurationParameters(false)
                .configurationParameter("com.wire.qa.picklejar.steps.packages", "com.wire.qa.picklejar.engine.testdata.lifecycle.beforeeachstep")
                .configurationParameter("com.wire.qa.picklejar.features.package", "com.wire.qa.picklejar.engine.testdata.lifecycle.beforeeachstep")
                .selectors(selectPackage(""))
                .execute();
        results.testEvents().assertStatistics(stats -> stats.started(3).succeeded(3));
        assertThat(com.wire.qa.picklejar.engine.testdata.lifecycle.beforeeachstep.LifeCycle.counter).isEqualTo(4);
    }

    @Test
    public void afterEachStep() {
        EngineExecutionResults results = EngineTestKit
                .engine("picklejar-engine")
                .enableImplicitConfigurationParameters(false)
                .configurationParameter("com.wire.qa.picklejar.steps.packages", "com.wire.qa.picklejar.engine.testdata.lifecycle.aftereachstep")
                .configurationParameter("com.wire.qa.picklejar.features.package", "com.wire.qa.picklejar.engine.testdata.lifecycle.aftereachstep")
                .selectors(selectPackage(""))
                .execute();

        results.testEvents().assertStatistics(stats -> stats.started(3).succeeded(3));
        assertThat(com.wire.qa.picklejar.engine.testdata.lifecycle.aftereachstep.LifeCycle.counter).isEqualTo(4);
    }

}
