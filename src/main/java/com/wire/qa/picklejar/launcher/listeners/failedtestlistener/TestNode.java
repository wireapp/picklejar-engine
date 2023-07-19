package com.wire.qa.picklejar.launcher.listeners.failedtestlistener;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.TestIdentifier;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TestNode {

    private TestIdentifier identifier;
    private TestExecutionResult result;
    final Queue<ReportEntry> reports = new ConcurrentLinkedQueue<>();

    public TestNode(TestIdentifier identifier) {
        this.identifier = identifier;
    }

    public TestNode addReportEntry(ReportEntry reportEntry) {
        reports.add(reportEntry);
        return this;
    }

    public TestIdentifier getIdentifier() {
        return identifier;
    }

    public TestNode setResult(TestExecutionResult result) {
        this.result = result;
        return this;
    }

    public TestExecutionResult getResult() {
        return result;
    }

    public Iterable<? extends ReportEntry> getReports() {
        return this.reports;
    }
}
