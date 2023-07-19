package com.wire.qa.picklejar.launcher.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.wire.qa.picklejar.launcher.listeners.failedtestlistener.TestNode;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

/**
 * Reimplementation of https://github.com/apache/maven-surefire/pull/245
 */
public class FailedTestListener implements TestExecutionListener {

    private static Logger logger;

    static {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tF %1$tT] [%4$s]: %5$s%6$s%n");
        logger = Logger.getLogger(FailedTestListener.class.getName());
    }

    private final Map<String, TestNode> nodesByUniqueId = new ConcurrentHashMap<>();

    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        nodesByUniqueId.clear();
    }

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        printResult();
    }

    @Override
    public void executionStarted(TestIdentifier testIdentifier) {
        nodesByUniqueId.put(testIdentifier.getUniqueId(), new TestNode(testIdentifier));
    }

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        if (testExecutionResult.getStatus() == TestExecutionResult.Status.FAILED) {
            logger.info(() -> String.format("[FailedTestListener] Adding '%s' to failures", testIdentifier.getDisplayName()));
            nodesByUniqueId.get(testIdentifier.getUniqueId()).setResult(testExecutionResult);
        } else {
            logger.finest(() -> String.format("[FailedTestListener] Remove '%s'", testIdentifier.getUniqueId()));
            nodesByUniqueId.remove(testIdentifier.getUniqueId());
        }
    }

    @Override
    public void reportingEntryPublished(TestIdentifier testIdentifier, ReportEntry entry) {
        if (nodesByUniqueId.containsKey(testIdentifier.getUniqueId())) {
            nodesByUniqueId.get(testIdentifier.getUniqueId()).addReportEntry(entry);
        }
    }

    /**
     * @return Map of tests that failed.
     */
    public List<String> getIdentifierOfFailures() {
        return new ArrayList<>(nodesByUniqueId.keySet());
    }

    public boolean hasFailingTests() {
        return !nodesByUniqueId.isEmpty();
    }

    private void printResult() {
        logger.info("");
        logger.info("-------------------< TEST FAILURES >-------------------");
        logger.info("");
        nodesByUniqueId.forEach((uniqueId, testNode) -> {
            logger.info(testNode.getIdentifier().getTags().stream().map(TestTag::getName).collect(Collectors.joining(" ")));
            logger.info(() -> "Scenario: " + testNode.getIdentifier().getDisplayName());
            for (ReportEntry reportEntry: testNode.getReports()) {
                if (reportEntry.getKeyValuePairs().keySet().stream().anyMatch(key -> key.equals("step"))) {
                    String step = reportEntry.getKeyValuePairs().get("step");
                    String status = reportEntry.getKeyValuePairs().get("status");
                    if (status.equals("skipped") || status.equals("passed") || status.equals("failed")) {
                        logger.info(() -> "  " + status.toUpperCase() + " " + step);
                    }
                }
            }
            if (testNode.getResult() != null && testNode.getResult().getThrowable().isPresent()) {
                Throwable throwable = testNode.getResult().getThrowable().get();
                logger.info(() -> "    Exception: " + throwable.getMessage());
                logger.log(Level.INFO, "    Stacktrace:", throwable.getCause());
            }
            logger.info(() -> "");
        });
    }

    public void clear() {
        nodesByUniqueId.clear();
    }
}
