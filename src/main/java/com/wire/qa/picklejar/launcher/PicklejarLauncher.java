package com.wire.qa.picklejar.launcher;

import java.io.File;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import com.wire.qa.picklejar.engine.PicklejarConfiguration;
import com.wire.qa.picklejar.engine.PicklejarEngine;
import com.wire.qa.picklejar.launcher.listeners.CucumberReportGeneratingListener;
import com.wire.qa.picklejar.launcher.listeners.FailedTestListener;
import com.wire.qa.picklejar.launcher.listeners.LegacyXmlReportGeneratingListener;
import org.junit.platform.launcher.*;
import org.junit.platform.launcher.core.LauncherConfig;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

public class PicklejarLauncher {

    private static final Logger logger = Logger.getLogger(PicklejarLauncher.class.getName());

    static final String target;

    static {
        try {
            target = new File(Objects.requireNonNull(PicklejarLauncher.class.getResource("/")).toURI()).getParent();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    static final Path xmlReportsDir = Paths.get(target, getPicklejarConfiguration().getXmlReportsDirectory());
    static final Path cucumberReportFile = Paths.get(target, getPicklejarConfiguration().getCucumberReportFileName());
    static final PrintWriter out = new PrintWriter(System.out);
    static final CucumberReportGeneratingListener cucumberReportGeneratingListener = new CucumberReportGeneratingListener(cucumberReportFile, out);
    static final FailedTestListener failedTestListener = new FailedTestListener();
    static final SummaryGeneratingListener summaryGeneratingListener = new SummaryGeneratingListener();
    static final LegacyXmlReportGeneratingListener legacyXmlReportListener = new LegacyXmlReportGeneratingListener(xmlReportsDir, out);

    public static void main(String[] args) {
        LauncherConfig launcherConfig = LauncherConfig.builder()
                .enableTestEngineAutoRegistration(false)
                .enableTestExecutionListenerAutoRegistration(false)
                .addTestEngines(new PicklejarEngine(cucumberReportGeneratingListener))
                .build();

        Launcher launcher = LauncherFactory.create(launcherConfig);
        LauncherDiscoveryRequestBuilder builder = LauncherDiscoveryRequestBuilder.request();

        String testProperty = System.getProperty("test");
        String tags = System.getProperty("picklejar.tags");
        String excludeTags = System.getProperty("picklejar.exclude.tags");

        if (testProperty != null && !testProperty.isEmpty()) {
            // Deflake tests by parsing test property and filtering via uniqueIds
            List<String> uniqueIds = TestPropertyParser.parse(testProperty);
            logger.info(() -> String.format("[Deflake] Following %d test cases were selected:", uniqueIds.size()));
            for (String id : uniqueIds) {
                logger.info(id);
            }
            builder.selectors(selectPackage(getPicklejarConfiguration().getFeaturesPackageName()));
            builder.filters(UniqueIdFilter.includeIds(uniqueIds));
        } else {
            builder.selectors(selectPackage(getPicklejarConfiguration().getFeaturesPackageName()));
            if (tags != null) {
                builder.filters(TagFilter.includeTags(tags.split(",")));
            }
            if (excludeTags != null && !excludeTags.isEmpty()) {
                builder.filters(TagFilter.excludeTags(excludeTags.split(",")));
            }
        }

        launcher.execute(builder.build(), legacyXmlReportListener, cucumberReportGeneratingListener, failedTestListener,
                summaryGeneratingListener);

        // Support for rerunning failed tests
        // Reimplementation of https://github.com/apache/maven-surefire/pull/245
        String rerunFailingTestsCountProperty = System.getProperty("surefire.rerunFailingTestsCount");

        long initialTestsFoundCount = summaryGeneratingListener.getSummary().getTestsFoundCount();
        long initialTestsFailedCount = summaryGeneratingListener.getSummary().getTestsFailedCount();
        long initialTestsSkippedCount = summaryGeneratingListener.getSummary().getTestsAbortedCount();
        long rerunTestsCount = 0;
        long rerunTestsFailed = 0;
        long finalTestsFailed = initialTestsFailedCount;

        if (rerunFailingTestsCountProperty != null) {

            int count = Integer.parseInt(rerunFailingTestsCountProperty);

            if (count > 0 && failedTestListener.hasFailingTests()) {
                for (int i = 0; i < count; i++) {
                    // Replace the "request" so that it only specifies the failing tests
                    LauncherDiscoveryRequest request = buildLauncherDiscoveryRequestForRerunFailures(failedTestListener);
                    // Reset adapter's recorded failures and invoke the failed tests again
                    failedTestListener.clear();
                    launcher.execute(request, legacyXmlReportListener, cucumberReportGeneratingListener,
                            failedTestListener, summaryGeneratingListener);
                    rerunTestsCount = rerunTestsCount + summaryGeneratingListener.getSummary().getTestsFoundCount();
                    rerunTestsFailed = rerunTestsFailed + summaryGeneratingListener.getSummary().getTestsFailedCount();
                    finalTestsFailed = summaryGeneratingListener.getSummary().getTestsFailedCount();
                    // If no tests fail in the rerun, we're done
                    if (finalTestsFailed == 0) {
                        break;
                    }
                }
            }
        }

        String line = String.format("[Results] Tests run: %d, Failures: %d, Rerun: %d, Flaky: %d, Skipped: %d",
                initialTestsFoundCount,
                finalTestsFailed,
                rerunTestsFailed,
                0,
                initialTestsSkippedCount
        );
        logger.info(String.join("", Collections.nCopies(line.length(), "-")));
        logger.info(line);
        logger.info(String.join("", Collections.nCopies(line.length(), "-")));

        // Fail build on test failures or maven.test.failure.ignore
        if (finalTestsFailed > 0) {
            if (System.getProperty("maven.test.failure.ignore") == null
                    || !Boolean.parseBoolean(System.getProperty("maven.test.failure.ignore"))) {
                System.exit(1);
            }
        }

        // We need to terminate manually here even on success because otherwise okio watchdog makes the maven process
        // hang because 'exec-maven-plugin' waits for all processes to finish in the VM
        // See: https://github.com/square/okio/issues/107 and https://github.com/square/okhttp/issues/6173
        System.exit(0);
    }

    private static LauncherDiscoveryRequest buildLauncherDiscoveryRequestForRerunFailures(FailedTestListener failedTestListener) {
        LauncherDiscoveryRequestBuilder builder = request();

        List<String> uniqueIds = new ArrayList<>();

        // Iterate over recorded failures
        for (String uniqueId : failedTestListener.getIdentifierOfFailures()) {
            // Add filter for the specific failing test
            logger.info("[Launcher] Rerun: " + uniqueId);
            uniqueIds.add(uniqueId);
        }

        builder.selectors(selectPackage(getPicklejarConfiguration().getFeaturesPackageName()));
        builder.filters(UniqueIdFilter.includeIds(uniqueIds));
        return builder.build();
    }

    private static PicklejarConfiguration getPicklejarConfiguration() {
        return new PicklejarConfiguration(request().build().getConfigurationParameters());
    }

}
