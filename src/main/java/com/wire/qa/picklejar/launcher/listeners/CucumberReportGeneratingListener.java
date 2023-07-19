package com.wire.qa.picklejar.launcher.listeners;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.wire.qa.picklejar.engine.descriptor.StepDescriptor;
import com.wire.qa.picklejar.engine.gherkin.model.CucumberReport;
import com.wire.qa.picklejar.engine.gherkin.model.Feature;
import com.wire.qa.picklejar.engine.gherkin.model.Result;
import com.wire.qa.picklejar.engine.gherkin.model.Scenario;
import com.wire.qa.picklejar.engine.gherkin.model.Tag;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;

public class CucumberReportGeneratingListener implements TestExecutionListener {

    private static final Logger logger = Logger.getLogger(CucumberReportGeneratingListener.class.getName());

    private final Path reportsFile;
    private final PrintWriter out;
    private Map<Feature, List<Scenario>> featureScenarioMap = new ConcurrentHashMap<>();

    public CucumberReportGeneratingListener(Path reportsFile, PrintWriter out) {
        this.reportsFile = reportsFile;
        this.out = out;
    }

    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        if(!reportsFile.getParent().toFile().exists()) {
            print("Could not find report directory: " + reportsFile.getParent());
        }
    }

    public void stepExecutionFinished(StepDescriptor descriptor, Result result) {
        logger.fine(() -> "[CucumberReport] executionFinished: " + descriptor.getDisplayName());
    }

    public void scenarioExecutionFinished(Scenario scenario) {
        logger.fine(() -> "[CucumberReport] executionFinished: " + scenario.getName());
        List<Scenario> scenariosForFeature = featureScenarioMap.getOrDefault(scenario.getFeature(), new ArrayList<>());
        scenariosForFeature.add(scenario);
        featureScenarioMap.put(scenario.getFeature(), scenariosForFeature);
    }

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        try {
            print("[CucumberReport] Test execution finished - Generating Cucumber report...");
            CucumberReport cucumberReport = new CucumberReport();
            for (Map.Entry<Feature, List<Scenario>> entry : featureScenarioMap.entrySet()) {
                Feature f = entry.getKey();
                // We write an empty list of tags and add them to the description instead because of the following bug
                // https://github.com/damianszczepanik/cucumber-reporting/issues/122
                List<Scenario> scenarios = entry.getValue();
                scenarios.forEach(s -> {
                    List<String> tags = s.getTags().stream().map(Tag::getName).collect(Collectors.toList());
                    s.setTags(Collections.EMPTY_LIST);
                    s.setDescription(s.getDescription() + String.join(" ", tags));
                });
                f.setScenarios(scenarios);
                cucumberReport.add(f);
            }
            cucumberReport.writeValue(reportsFile.toFile());
            print("[CucumberReport] Generating Cucumber report finished.");
        } catch (IOException e) {
            print("[CucumberReport] Generating Cucumber report failed.");
        }
    }

    private void print(String message) {
        logger.info(message);
    }
}
