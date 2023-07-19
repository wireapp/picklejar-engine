package com.wire.qa.picklejar.engine.tests;

import com.wire.qa.picklejar.engine.exception.SkipException;
import org.junit.jupiter.api.Test;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Execution;

import java.time.Duration;
import java.util.List;

import static com.wire.qa.picklejar.engine.tests.ReportEntryConditions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;
import static org.junit.platform.testkit.engine.EventConditions.*;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.instanceOf;

public class ScenarioTests {

    @Test
    public void simpleTestScenario() {
        EngineExecutionResults results = EngineTestKit
                .engine("picklejar-engine")
                .enableImplicitConfigurationParameters(false)
                .configurationParameter("com.wire.qa.picklejar.steps.packages", "com.wire.qa.picklejar.engine.testdata.scenario.simple")
                .configurationParameter("com.wire.qa.picklejar.features.package", "com.wire.qa.picklejar.engine.testdata.scenario.simple")
                .selectors(selectPackage(""))
                .execute();

        results.testEvents().assertStatistics(stats -> stats.started(1).succeeded(1));
        results.testEvents().succeeded().assertEventsMatchExactly(
                event(finishedSuccessfully(), displayName("Simple scenario"), uniqueIdSubstring("Simple scenario"))
        );
        results.allEvents().started().assertEventsMatchExactly(
                event(engine(), started()),
                event(displayName("Simple")),
                event(displayName("Simple scenario"))
        );
        results.allEvents().reportingEntryPublished().assertEventsMatchExactly(
                reportEntry("Simple step 1", "started"),
                reportEntry("Simple step 1", "passed"),
                reportEntry("Simple step 2", "started"),
                reportEntry("Simple step 2", "passed")
        );
    }

    @Test
    public void skipTest() {
        EngineExecutionResults results = EngineTestKit
                .engine("picklejar-engine")
                .enableImplicitConfigurationParameters(false)
                .configurationParameter("com.wire.qa.picklejar.steps.packages", "com.wire.qa.picklejar.engine.testdata.scenario.skiptest")
                .configurationParameter("com.wire.qa.picklejar.features.package", "com.wire.qa.picklejar.engine.testdata.scenario.skiptest")
                .selectors(selectPackage(""))
                .execute();

        results.testEvents().assertStatistics(stats -> stats.started(1).succeeded(1));
        results.allEvents().reportingEntryPublished().assertEventsMatchExactly(
                reportEntry("I skip the test on condition", "started"),
                reportEntry("I skip the test on condition", "skipped")
        );
        results.testEvents().finished().assertEventsMatchExactly(
                event(abortedWithReason(instanceOf(SkipException.class)), displayName("Skip test with special step")),
                event(finishedSuccessfully(), displayName("Skip test with special step"))
        );
    }

    @Test
    public void failTest() {
        EngineExecutionResults results = EngineTestKit
                .engine("picklejar-engine")
                .enableImplicitConfigurationParameters(false)
                .configurationParameter("com.wire.qa.picklejar.steps.packages", "com.wire.qa.picklejar.engine.testdata.scenario.failtest")
                .configurationParameter("com.wire.qa.picklejar.features.package", "com.wire.qa.picklejar.engine.testdata.scenario.failtest")
                .selectors(selectPackage(""))
                .execute();

        results.testEvents().assertStatistics(stats -> stats.started(1).failed(1));
        results.allEvents().reportingEntryPublished().assertEventsMatchExactly(
                reportEntry("I fail", "started"),
                reportEntry("I fail", "failed")
        );
        results.testEvents().finished().assertEventsMatchExactly(
                event(finishedWithFailure(instanceOf(RuntimeException.class)))
        );
    }

    @Test
    public void scenarioWithOutlineAndPlaceholders() {
        EngineExecutionResults results = EngineTestKit
                .engine("picklejar-engine")
                .enableImplicitConfigurationParameters(false)
                .configurationParameter("com.wire.qa.picklejar.steps.packages", "com.wire.qa.picklejar.engine.testdata.scenario.outline")
                .configurationParameter("com.wire.qa.picklejar.features.package", "com.wire.qa.picklejar.engine.testdata.scenario.outline")
                .selectors(selectPackage(""))
                .execute();

        results.testEvents().assertStatistics(stats -> stats.started(1).succeeded(1));
        results.testEvents().succeeded().assertEventsMatchExactly(
                event(finishedSuccessfully(), displayName("Scenario With Outline (Examples) 0"))
        );
        results.allEvents().started().assertEventsMatchExactly(
                event(engine(), started()),
                event(displayName("Scenario With Outline Examples"), uniqueIdSubstring("Scenario With Outline Examples")),
                event(displayName("Scenario With Outline (Examples) 0"), uniqueIdSubstring("0"))
        );
        results.allEvents().reportingEntryPublished().assertEventsMatchExactly(
                reportEntry("Step which uses placeholder Value1 from Examples", "started"),
                reportEntry("Step which uses placeholder Value1 from Examples", "passed"),
                reportEntry("Step which uses placeholder Value2 from Examples", "started"),
                reportEntry("Step which uses placeholder Value2 from Examples", "passed")
        );
    }

    @Test
    public void scenarioWithTwoExamples() {
        EngineExecutionResults results = EngineTestKit
                .engine("picklejar-engine")
                .enableImplicitConfigurationParameters(false)
                .configurationParameter("com.wire.qa.picklejar.steps.packages", "com.wire.qa.picklejar.engine.testdata.scenario.twoexamples")
                .configurationParameter("com.wire.qa.picklejar.features.package", "com.wire.qa.picklejar.engine.testdata.scenario.twoexamples")
                .selectors(selectPackage(""))
                .execute();

        results.testEvents().assertStatistics(stats -> stats.started(2).succeeded(2));
        results.testEvents().succeeded().assertEventsMatchExactly(
                event(finishedSuccessfully(), displayName("Scenario With Two Examples 0")),
                event(finishedSuccessfully(), displayName("Scenario With Two Examples 1"))
        );
        results.allEvents().reportingEntryPublished().assertEventsMatchExactly(
                reportEntry("Step which uses placeholder Value1 from Examples", "started"),
                reportEntry("Step which uses placeholder Value1 from Examples", "passed"),
                reportEntry("Step which uses placeholder Value2 from Examples", "started"),
                reportEntry("Step which uses placeholder Value2 from Examples", "passed")
        );
    }

    @Test
    public void scenariosRunSequentialWithResourceLock() {
        EngineExecutionResults results = EngineTestKit
                .engine("picklejar-engine")
                .enableImplicitConfigurationParameters(false)
                .configurationParameter("com.wire.qa.picklejar.steps.packages", "com.wire.qa.picklejar.engine.testdata.scenario.resourcelock")
                .configurationParameter("com.wire.qa.picklejar.features.package", "com.wire.qa.picklejar.engine.testdata.scenario.resourcelock")
                .configurationParameter("picklejar.parallelism", "2")
                .configurationParameter("junit.jupiter.execution.parallel.enabled", "true")
                .selectors(selectPackage(""))
                .execute();

        results.testEvents().assertStatistics(stats -> stats.started(2).succeeded(2));

        List<Execution> executions = results.allEvents().executions().list();
        // check that execution 1 ended before execution 2 started
        System.out.println(String.format("Execution 1: %s (started %s)",
                executions.get(0).getTestDescriptor().getDisplayName(),
                executions.get(0).getStartInstant()));
        System.out.println(String.format("Execution 2: %s (started %s)",
                executions.get(1).getTestDescriptor().getDisplayName(),
                executions.get(1).getStartInstant()));
        assertThat(executions.get(0).getEndInstant()).isBefore(executions.get(1).getStartInstant());
    }

    @Test
    public void scenariosRunParallelWithoutResourceLock() {
        EngineExecutionResults results = EngineTestKit
                .engine("picklejar-engine")
                .enableImplicitConfigurationParameters(false)
                .configurationParameter("com.wire.qa.picklejar.steps.packages", "com.wire.qa.picklejar.engine.testdata.scenario.parallel")
                .configurationParameter("com.wire.qa.picklejar.features.package", "com.wire.qa.picklejar.engine.testdata.scenario.parallel")
                .configurationParameter("picklejar.parallelism", "2")
                .configurationParameter("junit.jupiter.execution.parallel.enabled", "true")
                .selectors(selectPackage(""))
                .execute();

        results.testEvents().assertStatistics(stats -> stats.started(2).succeeded(2));

        List<Execution> executions = results.allEvents().executions().list();
        // check that execution 1 started at the same time as execution 2
        System.out.println(String.format("Execution 1: %s (started %s)",
                executions.get(0).getTestDescriptor().getDisplayName(),
                executions.get(0).getStartInstant()));
        System.out.println(String.format("Execution 2: %s (started %s)",
                executions.get(1).getTestDescriptor().getDisplayName(),
                executions.get(1).getStartInstant()));
        Duration duration = Duration.between(executions.get(0).getStartInstant(), executions.get(1).getStartInstant());
        long difference = duration.toMillis();
        assertThat(Math.abs(difference)).isLessThanOrEqualTo(100);
    }

}
