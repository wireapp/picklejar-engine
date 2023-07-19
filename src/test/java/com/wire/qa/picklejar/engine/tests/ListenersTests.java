package com.wire.qa.picklejar.engine.tests;

import com.wire.qa.picklejar.engine.gherkin.model.Feature;
import com.wire.qa.picklejar.engine.gherkin.model.Scenario;
import com.wire.qa.picklejar.engine.gherkin.model.Step;
import com.wire.qa.picklejar.engine.gherkin.model.Tag;
import com.wire.qa.picklejar.launcher.listeners.CucumberReportGeneratingListener;
import com.wire.qa.picklejar.launcher.listeners.FailedTestListener;
import com.wire.qa.picklejar.launcher.listeners.LegacyXmlReportGeneratingListener;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.engine.TestExecutionResult.failed;
import static org.junit.platform.engine.TestExecutionResult.successful;
import static org.mockito.Mockito.mock;

public class ListenersTests {

    private final EngineDescriptor engineDescriptor = new EngineDescriptor(UniqueId.forEngine("engine"), "Engine");

    private static class TestDescriptorStub extends AbstractTestDescriptor {

        public TestDescriptorStub(UniqueId uniqueId, String displayName) {
            super(uniqueId, displayName);
        }

        @Override
        public Type getType() {
            return getChildren().isEmpty() ? Type.TEST : Type.CONTAINER;
        }

    }

    @Test
    public void cucumberReportGeneratingListener() throws IOException {
        UniqueId uniqueId = engineDescriptor.getUniqueId().append("test", "test");
        TestDescriptorStub testDescriptor = new TestDescriptorStub(uniqueId, "successfulTest");
        engineDescriptor.addChild(testDescriptor);
        Set<TestDescriptor> set = new HashSet<>();
        set.add(engineDescriptor);
        ConfigurationParameters configurationParameters = mock(ConfigurationParameters.class);
        TestPlan testPlan = TestPlan.from(set, configurationParameters);

        Path tempFile = Files.createTempFile(null, ".json");

        PrintWriter out = new PrintWriter(System.out);
        CucumberReportGeneratingListener listener = new CucumberReportGeneratingListener(tempFile, out);

        listener.testPlanExecutionStarted(testPlan);
        Feature feature = new Feature("Featurename");
        Step step = new Step("Given", "I do something");
        List<Step> steps = new ArrayList<>();
        steps.add(step);
        List<Tag> tags = new ArrayList<>();
        tags.add(new Tag("@smoke"));
        Scenario scenario = new Scenario(feature, "Scenarioname", "@C1234 iPhone X (9)", 0, null, steps, tags);
        listener.scenarioExecutionFinished(scenario);
        listener.testPlanExecutionFinished(testPlan);

        String text = new Scanner(tempFile).useDelimiter("\n").next();
        JSONArray array = new JSONArray(text);
        assertThat(array.length()).isEqualTo(1);
        JSONObject object = array.getJSONObject(0);
        assertThat(object.getString("name")).isEqualTo("Featurename");
        assertThat(object.getString("keyword")).isEqualTo("Feature");
        assertThat(object.getJSONArray("elements").length()).isEqualTo(1);
        JSONArray elementsArray = object.getJSONArray("elements");
        assertThat(elementsArray.length()).isEqualTo(1);
        JSONObject elementsObject = elementsArray.getJSONObject(0);
        assertThat(elementsObject.getString("name")).isEqualTo("Scenarioname");
        JSONArray stepsArray = elementsObject.getJSONArray("steps");
        assertThat(stepsArray.length()).isEqualTo(1);
        JSONObject stepObject = stepsArray.getJSONObject(0);
        assertThat(stepObject.getString("name")).isEqualTo("I do something");
    }

    @Test
    public void failedTestListener() {
        UniqueId uniqueIdFailed = engineDescriptor.getUniqueId().append("test", "test1");
        UniqueId uniqueIdSuccess = engineDescriptor.getUniqueId().append("test", "test2");
        TestDescriptorStub testDescriptorFailed = new TestDescriptorStub(uniqueIdFailed, "failedTest");
        TestDescriptorStub testDescriptorSuccess = new TestDescriptorStub(uniqueIdSuccess, "successfulTest");
        engineDescriptor.addChild(testDescriptorFailed);
        engineDescriptor.addChild(testDescriptorSuccess);
        Set<TestDescriptor> set = new HashSet<>();
        set.add(engineDescriptor);
        ConfigurationParameters configurationParameters = mock(ConfigurationParameters.class);
        TestPlan testPlan = TestPlan.from(set, configurationParameters);

        FailedTestListener listener = new FailedTestListener();
        assertThat(listener.hasFailingTests()).isEqualTo(false);

        listener.testPlanExecutionStarted(testPlan);
        TestIdentifier identifier = TestIdentifier.from(testDescriptorFailed);
        listener.executionStarted(identifier);
        listener.reportingEntryPublished(identifier, ReportEntry.from("step", "This is a step"));
        TestExecutionResult failure = failed(new RuntimeException("Exception 1"));
        listener.executionFinished(identifier, failure);
        listener.executionFinished(TestIdentifier.from(testDescriptorSuccess), successful());

        assertThat(listener.hasFailingTests()).isEqualTo(true);
        assertThat(listener.getIdentifierOfFailures())
                .contains(identifier.getUniqueId());
    }

    @Test
    public void legacyXmlReportGeneratingListener() throws IOException, ParserConfigurationException, SAXException {
        UniqueId uniqueId1 = engineDescriptor.getUniqueId()
                .append("Feature", "Feature1")
                .append("Scenario", "Scenario1OfFeature1");
        UniqueId uniqueId2 = engineDescriptor.getUniqueId()
                .append("Feature", "Feature2")
                .append("Scenario", "Scenario1OfFeature2");
        UniqueId uniqueId3 = engineDescriptor.getUniqueId()
                .append("Feature", "Feature2")
                .append("Scenario", "Scenario2OfFeature2");
        TestDescriptorStub testDescriptor1 = new TestDescriptorStub(uniqueId1, "Scenario1OfFeature1");
        TestDescriptorStub testDescriptor2 = new TestDescriptorStub(uniqueId2, "Scenario1OfFeature2");
        TestDescriptorStub testDescriptor3 = new TestDescriptorStub(uniqueId3, "Scenario2OfFeature2");
        engineDescriptor.addChild(testDescriptor1);
        engineDescriptor.addChild(testDescriptor2);
        engineDescriptor.addChild(testDescriptor3);
        Set<TestDescriptor> set = new HashSet<>();
        set.add(engineDescriptor);
        ConfigurationParameters configurationParameters = mock(ConfigurationParameters.class);
        TestPlan testPlan = TestPlan.from(set, configurationParameters);

        Path tempDir = Files.createTempDirectory("xml-reports-");
        System.out.println("tempDir: " + tempDir);
        PrintWriter out = new PrintWriter(System.out);
        LegacyXmlReportGeneratingListener listener = new LegacyXmlReportGeneratingListener(tempDir, out);

        listener.testPlanExecutionStarted(testPlan);
        assertThat(tempDir).exists();

        TestIdentifier identifier1 = TestIdentifier.from(testDescriptor1);
        TestIdentifier identifier2 = TestIdentifier.from(testDescriptor2);
        TestIdentifier identifier3 = TestIdentifier.from(testDescriptor3);
        listener.executionStarted(identifier1);
        listener.executionFinished(identifier1, successful());
        listener.executionStarted(identifier2);
        listener.executionFinished(identifier2, successful());
        listener.executionStarted(identifier3);
        listener.executionFinished(identifier3, failed(new RuntimeException("Exception 1")));
        listener.testPlanExecutionFinished(testPlan);

        String xmlFile1 = "TEST-Feature1.Scenario1OfFeature1 0.xml";
        String xmlFile2 = "TEST-Feature2.Scenario1OfFeature2 0.xml";
        String xmlFile3 = "TEST-Feature2.Scenario2OfFeature2 0.xml";

        assertThat(tempDir)
                .isDirectoryContaining(file -> file.toFile().getName().equals(xmlFile1))
                .isDirectoryContaining(file -> file.toFile().getName().equals(xmlFile2))
                .isDirectoryContaining(file -> file.toFile().getName().equals(xmlFile3));

        // Parse XML files
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new File(tempDir + File.separator + xmlFile1));
        doc.getDocumentElement().normalize();
        NodeList list = doc.getElementsByTagName("testcase");
        assertThat(list.getLength()).isEqualTo(1);

        Node node = list.item(0);
        Element element = (Element) node;
        assertThat(element.getAttribute("name")).isEqualTo("Scenario1OfFeature1");
    }

    @Test
    public void legacyXmlReportGeneratingListenerWithIllegalCharacters() throws IOException {
        UniqueId uniqueId = engineDescriptor.getUniqueId()
                .append("Feature", "Feature with")
                .append("Scenario", "Scenario with #/:`'\"[]");
        TestDescriptorStub testDescriptor = new TestDescriptorStub(uniqueId, "Scenario with #/:`'\"[]");
        engineDescriptor.addChild(testDescriptor);
        Set<TestDescriptor> set = new HashSet<>();
        set.add(engineDescriptor);
        ConfigurationParameters configurationParameters = mock(ConfigurationParameters.class);
        TestPlan testPlan = TestPlan.from(set, configurationParameters);

        Path tempDir = Files.createTempDirectory("xml-reports-");
        System.out.println("tempDir: " + tempDir);
        PrintWriter out = new PrintWriter(System.out);
        LegacyXmlReportGeneratingListener listener = new LegacyXmlReportGeneratingListener(tempDir, out);

        listener.testPlanExecutionStarted(testPlan);
        assertThat(tempDir).exists();

        TestIdentifier identifier = TestIdentifier.from(testDescriptor);
        listener.executionStarted(identifier);
        listener.executionFinished(identifier, successful());
        listener.testPlanExecutionFinished(testPlan);

        String xmlFile = "TEST-Feature with.Scenario with 0.xml";

        assertThat(tempDir)
                .isDirectoryContaining(file -> file.toFile().getName().equals(xmlFile));
    }

    @Test
    public void legacyXmlReportGeneratingListenerWithSlash() throws IOException {
        UniqueId uniqueId = engineDescriptor.getUniqueId()
                .append("Feature", "Feature with /")
                .append("Scenario", "Scenario with #/:`'\"[]");
        TestDescriptorStub testDescriptor = new TestDescriptorStub(uniqueId, "Scenario with #/:`'\"[]");
        engineDescriptor.addChild(testDescriptor);
        Set<TestDescriptor> set = new HashSet<>();
        set.add(engineDescriptor);
        ConfigurationParameters configurationParameters = mock(ConfigurationParameters.class);
        TestPlan testPlan = TestPlan.from(set, configurationParameters);

        Path tempDir = Files.createTempDirectory("xml-reports-");
        System.out.println("tempDir: " + tempDir);
        PrintWriter out = new PrintWriter(System.out);
        LegacyXmlReportGeneratingListener listener = new LegacyXmlReportGeneratingListener(tempDir, out);

        listener.testPlanExecutionStarted(testPlan);
        assertThat(tempDir).exists();

        TestIdentifier identifier = TestIdentifier.from(testDescriptor);
        listener.executionStarted(identifier);
        listener.executionFinished(identifier, successful());
        listener.testPlanExecutionFinished(testPlan);

        String xmlFile = "TEST-Feature with  .Scenario with 0.xml";

        assertThat(tempDir)
                .isDirectoryContaining(file -> file.toFile().getName().equals(xmlFile));
    }

}
