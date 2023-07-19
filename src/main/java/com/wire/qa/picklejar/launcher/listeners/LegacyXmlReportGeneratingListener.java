/*
 * Copyright 2015-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package com.wire.qa.picklejar.launcher.listeners;

import static org.apiguardian.api.API.Status.STABLE;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;

import org.apiguardian.api.API;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

/**
 * {@code LegacyXmlReportGeneratingListener} is a {@link TestExecutionListener} that
 * generates a separate XML report for each {@linkplain TestPlan#getRoots() root}
 * in the {@link TestPlan}.
 *
 * <p>Note that the generated XML format is compatible with the <em>legacy</em>
 * de facto standard for JUnit 4 based test reports that was made popular by the
 * Ant build system.
 *
 * @since 1.4
 * @see org.junit.platform.launcher.listeners.LoggingListener
 * @see org.junit.platform.launcher.listeners.SummaryGeneratingListener
 */
@API(status = STABLE, since = "1.7")
public class LegacyXmlReportGeneratingListener implements TestExecutionListener {

    private static final Logger logger = Logger.getLogger(LegacyXmlReportGeneratingListener.class.getName());

    private final Path reportsDir;
    private final PrintWriter out;
    private final Clock clock;

    private XmlReportData reportData;

    public LegacyXmlReportGeneratingListener(Path reportsDir, PrintWriter out) {
        this(reportsDir, out, Clock.systemDefaultZone());
    }

    // For tests only
    LegacyXmlReportGeneratingListener(String reportsDir, PrintWriter out, Clock clock) {
        this(Paths.get(reportsDir), out, clock);
    }

    private LegacyXmlReportGeneratingListener(Path reportsDir, PrintWriter out, Clock clock) {
        this.reportsDir = reportsDir;
        this.out = out;
        this.clock = clock;
    }

    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        this.reportData = new XmlReportData(testPlan, clock);
        try {
            Files.createDirectories(this.reportsDir);
        }
        catch (IOException e) {
            printException("Could not create reports directory: " + this.reportsDir, e);
        }
    }

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        this.reportData = null;
    }

    @Override
    public void executionSkipped(TestIdentifier testIdentifier, String reason) {
        this.reportData.markSkipped(testIdentifier, reason);
        writeXmlReportInCaseOfRoot(testIdentifier);
    }

    @Override
    public void executionStarted(TestIdentifier testIdentifier) {
        this.reportData.markStarted(testIdentifier);
    }

    @Override
    public void reportingEntryPublished(TestIdentifier testIdentifier, ReportEntry entry) {
        this.reportData.addReportEntry(testIdentifier, entry);
    }

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult result) {
        logger.info(() -> "[LegacyXMLReport] executionFinished: '" + testIdentifier.getDisplayName()
                + "' " + result.getStatus().name());
        this.reportData.markFinished(testIdentifier, result);
        writeXmlReportInCaseOfRoot(testIdentifier);
    }

    private void writeXmlReportInCaseOfRoot(TestIdentifier testIdentifier) {
        // Change in LegacyXmlReportGeneratingListener b/c of https://github.com/junit-team/junit5/issues/1989
        if (testIdentifier.getType().isTest()) {
            UniqueId uniqueId = UniqueId.parse(testIdentifier.getUniqueId());
            String name = getFeature(uniqueId) + "." + getScenario(uniqueId) + " " + getExample(uniqueId);
            name = name.replaceAll(Pattern.quote(File.separator), " ");
            logger.info(() -> "[LegacyXMLReport] Write XML Report...");
            writeXmlReportSafely(reportData.getTestPlan().getParent(testIdentifier).get(), testIdentifier, name);
        }
    }

    private String getFeature(UniqueId uniqueId) {
        for (UniqueId.Segment segment : uniqueId.getSegments()) {
            if (segment.getType().equals("Feature")) {
                return segment.getValue();
            }
        }
        return null;
    }

    private String getScenario(UniqueId uniqueId) {
        for (UniqueId.Segment segment : uniqueId.getSegments()) {
            if (segment.getType().equals("Scenario")) {
                return normalize(segment.getValue());
            }
        }
        return null;
    }

    private String normalize(String text) {
        return text
                .replaceAll("[^a-zA-Z0-9 ]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private int getExample(UniqueId uniqueId) {
        for (UniqueId.Segment segment : uniqueId.getSegments()) {
            if (segment.getType().equals("Example")) {
                return Integer.parseInt(segment.getValue());
            }
        }
        return 0;
    }

    private void writeXmlReportSafely(TestIdentifier rootDescriptor, TestIdentifier testIdentifier, String rootName) {
        Path xmlFile = this.reportsDir.resolve("TEST-" + rootName + ".xml");
        try (Writer fileWriter = Files.newBufferedWriter(xmlFile)) {
            new XmlReportWriter(this.reportData).writeXmlReport(rootDescriptor, testIdentifier, fileWriter);
        }
        catch (XMLStreamException | IOException e) {
            printException("Could not write XML report: " + xmlFile, e);
        }
    }

    private boolean isRoot(TestIdentifier testIdentifier) {
        return !testIdentifier.getParentId().isPresent();
    }

    private void printException(String message, Exception exception) {
        out.println(message);
        exception.printStackTrace(out);
    }

}