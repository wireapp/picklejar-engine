package com.wire.qa.picklejar.engine.tests;

import com.wire.qa.picklejar.engine.annotations.AnnotationPattern;
import com.wire.qa.picklejar.engine.discovery.FeatureSelectorResolver;
import com.wire.qa.picklejar.engine.exception.DiscoveryException;
import com.wire.qa.picklejar.engine.exception.MethodForStepNotFoundException;
import com.wire.qa.picklejar.engine.exception.MisconfigurationException;
import com.wire.qa.picklejar.launcher.UniqueIdFilter;
import gherkin.ParserException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.TagFilter;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static com.wire.qa.picklejar.engine.tests.ReportEntryConditions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.platform.engine.discovery.DiscoverySelectors.*;
import static org.junit.platform.testkit.engine.EventConditions.*;
import static org.junit.platform.testkit.engine.EventConditions.displayName;

public class DiscoveryTests {

    @Test
    public void featureTag() {
        EngineExecutionResults results = EngineTestKit
                .engine("picklejar-engine")
                .enableImplicitConfigurationParameters(false)
                .configurationParameter("com.wire.qa.picklejar.steps.packages", "com.wire.qa.picklejar.engine.testdata.steps")
                .configurationParameter("com.wire.qa.picklejar.features.package", "com.wire.qa.picklejar.engine.testdata.discovery.tags")
                .selectors(selectPackage(""))
                .filters(TagFilter.includeTags("@featuretag"))
                .execute();

        results.testEvents().assertStatistics(stats -> stats.started(3).succeeded(3));
        results.testEvents().succeeded().assertEventsMatchExactly(
                event(displayName("Scenario 1")),
                event(displayName("Scenario 2")),
                event(displayName("Scenario 3"))
        );
    }

    @Test
    public void scenarioTag() {
        EngineExecutionResults results = EngineTestKit
                .engine("picklejar-engine")
                .enableImplicitConfigurationParameters(false)
                .configurationParameter("com.wire.qa.picklejar.steps.packages", "com.wire.qa.picklejar.engine.testdata.steps")
                .configurationParameter("com.wire.qa.picklejar.features.package", "com.wire.qa.picklejar.engine.testdata.discovery.tags")
                .selectors(selectPackage(""))
                .filters(TagFilter.includeTags("@scenariotag1"))
                .execute();

        results.testEvents().assertStatistics(stats -> stats.started(1).succeeded(1));
        results.testEvents().succeeded().assertEventsMatchExactly(
                event(displayName("Scenario 1"))
        );
    }

    @Test
    public void multipleScenarioTags() {
        EngineExecutionResults results = EngineTestKit
                .engine("picklejar-engine")
                .enableImplicitConfigurationParameters(false)
                .configurationParameter("com.wire.qa.picklejar.steps.packages", "com.wire.qa.picklejar.engine.testdata.steps")
                .configurationParameter("com.wire.qa.picklejar.features.package", "com.wire.qa.picklejar.engine.testdata.discovery.tags")
                .selectors(selectPackage(""))
                .filters(TagFilter.includeTags("@scenariotag1", "@scenariotag2"))
                .execute();

        results.testEvents().assertStatistics(stats -> stats.started(2).succeeded(2));
        results.testEvents().succeeded().assertEventsMatchExactly(
                event(displayName("Scenario 1")),
                event(displayName("Scenario 2"))
        );
    }

    @Test
    public void filterByUniqueId() {
        EngineExecutionResults results = EngineTestKit
                .engine("picklejar-engine")
                .enableImplicitConfigurationParameters(false)
                .configurationParameter("com.wire.qa.picklejar.steps.packages", "com.wire.qa.picklejar.engine.testdata.steps")
                .configurationParameter("com.wire.qa.picklejar.features.package", "com.wire.qa.picklejar.engine.testdata.discovery.uniqueid")
                .selectors(selectPackage(""))
                .filters(UniqueIdFilter.includeIds("[engine:picklejar-engine]/[Feature:Unique Id]/[Scenario:Simple Scenario]"))
                .execute();

        results.testEvents().assertStatistics(stats -> stats.started(1).succeeded(1));
        results.testEvents().succeeded().assertEventsMatchExactly(
                event(displayName("Simple Scenario"))
        );
    }

    @Test
    public void filterByUniqueIdWithSubdirectory() {
        EngineExecutionResults results = EngineTestKit
                .engine("picklejar-engine")
                .enableImplicitConfigurationParameters(false)
                .configurationParameter("com.wire.qa.picklejar.steps.packages", "com.wire.qa.picklejar.engine.testdata.steps")
                .configurationParameter("com.wire.qa.picklejar.features.package", "com.wire.qa.picklejar.engine.testdata.discovery.uniqueidwithsubdirectory")
                .selectors(selectPackage(""))
                .filters(UniqueIdFilter.includeIds("[engine:picklejar-engine]/[Feature:subdirectory%2FUnique Id]/[Scenario:Simple Scenario]"))
                .execute();

        results.testEvents().assertStatistics(stats -> stats.started(1).succeeded(1));
        results.testEvents().succeeded().assertEventsMatchExactly(
                event(displayName("Simple Scenario"))
        );
    }

    @Test
    public void uniqueId() {
        EngineExecutionResults results = EngineTestKit
                .engine("picklejar-engine")
                .enableImplicitConfigurationParameters(false)
                .configurationParameter("com.wire.qa.picklejar.steps.packages", "com.wire.qa.picklejar.engine.testdata.steps")
                .configurationParameter("com.wire.qa.picklejar.features.package", "com.wire.qa.picklejar.engine.testdata.discovery.uniqueid")
                .selectors(selectUniqueId("[engine:picklejar-engine]/[Feature:Unique Id]/[Scenario:Simple Scenario]"))
                .filters(UniqueIdFilter.includeIds("[engine:picklejar-engine]/[Feature:Unique Id]/[Scenario:Simple Scenario]"))
                .execute();

        results.testEvents().assertStatistics(stats -> stats.started(1).succeeded(1));
        results.testEvents().succeeded().assertEventsMatchExactly(
                event(displayName("Simple Scenario"))
        );
    }

    /**
     * Surefire starts test with ClassSelector but no configuration. This should just be skipped and not result in real
     * discovery because otherwise the tests of the test engine itself fail with SurefireBooterForkException
     */
    @Test
    public void mitigateDiscoveryIssueFromSurefire() {
        EngineExecutionResults results = EngineTestKit
                .engine("picklejar-engine")
                .enableImplicitConfigurationParameters(false)
                .selectors(selectClass("com.wire.qa.picklejar.engine.tests.DiscoveryTests"))
                .execute();

        results.testEvents().assertStatistics(stats -> stats.started(0));
    }

    /**
     * When using JUnit run configuration in IntelliJ the tests are started without configuration.
     */
    @Test
    public void supportIntelliJRunConfiguration() {
        Exception exception = assertThrows(JUnitException.class, () -> {
            EngineTestKit
                    .engine("picklejar-engine")
                    .enableImplicitConfigurationParameters(false)
                    .selectors(selectPackage(""))
                    .execute();
        });
        assertEquals(MisconfigurationException.class,
                exception.getCause().getClass());
        assertEquals("Please add property com.wire.qa.picklejar.steps.packages with package name containing steps classes (if multiple separate by comma)",
                exception.getCause().getMessage());
    }

    @Test
    public void packagePathForFeaturesIsNotConfigured() {
        Exception exception = assertThrows(JUnitException.class, () -> {
            EngineTestKit
                    .engine("picklejar-engine")
                    .enableImplicitConfigurationParameters(false)
                    .configurationParameter("com.wire.qa.picklejar.steps.packages", "com.wire.qa.picklejar.engine.testdata.steps")
                    .selectors(selectPackage(""))
                    .execute();
        });
        assertEquals(JUnitException.class,
                exception.getCause().getClass());
        assertEquals("PackageSelector [packageName = ''] resolution failed",
                exception.getCause().getMessage());
        assertEquals(MisconfigurationException.class,
                exception.getCause().getCause().getClass());
        assertEquals("Please add property com.wire.qa.picklejar.features.package with package name of resources which contain feature files",
                exception.getCause().getCause().getMessage());
    }

    @Test
    @Disabled
    public void packagePathForStepsIsNotConfigured() {
        // FIXME: This test should throw a MisconfigurationException to show that com.wire.qa.picklejar.steps.packages is missing
        Exception exception = assertThrows(JUnitException.class, () -> EngineTestKit
                .engine("picklejar-engine")
                .enableImplicitConfigurationParameters(false)
                .selectors(selectPackage("com.wire.qa.picklejar.engine.testdata.lifecycle.beforeeachscenario"))
                .execute());

        assertEquals(JUnitException.class,
                exception.getCause().getClass());
        assertEquals("PackageSelector [packageName = ''] resolution failed",
                exception.getCause().getMessage());
        assertEquals(MisconfigurationException.class,
                exception.getCause().getCause().getClass());
        assertEquals("Please add property com.wire.qa.picklejar.features.package with package name of resources which contain feature files",
                exception.getCause().getCause().getMessage());
    }

    @Test
    public void cannotFindFeaturePackageConfiguredViaParameter() {
        Exception exception = assertThrows(JUnitException.class, () -> {
            EngineTestKit
                    .engine("picklejar-engine")
                    .enableImplicitConfigurationParameters(false)
                    .configurationParameter("com.wire.qa.picklejar.steps.packages", "com.wire.qa.picklejar.engine.testdata.steps")
                    .configurationParameter("com.wire.qa.picklejar.features.package", "package.that.does.not.exist")
                    .selectors(selectPackage(""))
                    .execute();
        });
        assertEquals(JUnitException.class,
                exception.getCause().getClass());
        assertEquals("PackageSelector [packageName = ''] resolution failed",
                exception.getCause().getMessage());
        assertEquals(PreconditionViolationException.class,
                exception.getCause().getCause().getClass());
        assertEquals("Could not find files with extension '.feature' in any provided package: package.that.does.not.exist",
                exception.getCause().getCause().getMessage());
    }

    @Test
    public void cannotFindFeaturePackage() {
        Exception exception = assertThrows(JUnitException.class, () -> {
            EngineTestKit
                    .engine("picklejar-engine")
                    .enableImplicitConfigurationParameters(false)
                    .configurationParameter("com.wire.qa.picklejar.steps.packages", "com.wire.qa.picklejar.engine.testdata.steps")
                    .selectors(selectPackage("package.that.does.not.exist"))
                    .execute();
        });
        assertEquals(JUnitException.class,
                exception.getCause().getClass());
        assertEquals("PackageSelector [packageName = 'package.that.does.not.exist'] resolution failed",
                exception.getCause().getMessage());
        assertEquals(PreconditionViolationException.class,
                exception.getCause().getCause().getClass());
        assertEquals("Could not find files with extension '.feature' in any provided package: package.that.does.not.exist",
                exception.getCause().getCause().getMessage());
    }

    @Test
    public void cannotFindAnyFeatureFileInPackage() {
        Exception exception = assertThrows(JUnitException.class, () -> {
            EngineTestKit
                    .engine("picklejar-engine")
                    .enableImplicitConfigurationParameters(false)
                    .configurationParameter("com.wire.qa.picklejar.steps.packages", "com.wire.qa.picklejar.engine.testdata.steps")
                    .selectors(selectPackage("com.wire.qa.picklejar.engine.testdata.discovery.empty"))
                    .execute();
        });
        assertThat(exception.getCause().getClass())
                .isEqualTo(JUnitException.class);
        assertThat(exception.getCause().getMessage())
                .isEqualTo("PackageSelector [packageName = 'com.wire.qa.picklejar.engine.testdata.discovery.empty'] resolution failed");
        assertThat(exception.getCause().getCause().getClass())
                .isEqualTo(PreconditionViolationException.class);
        assertThat(exception.getCause().getCause().getMessage())
                .isEqualTo("Could not find files with extension '.feature' in any provided package: com.wire.qa.picklejar.engine.testdata.discovery.empty");
    }

    @Test
    public void cannotFindMethodForStep() {
        Exception exception = assertThrows(JUnitException.class, () -> EngineTestKit
                .engine("picklejar-engine")
                .enableImplicitConfigurationParameters(false)
                .configurationParameter("com.wire.qa.picklejar.steps.packages", "com.wire.qa.picklejar.engine.testdata.steps")
                .configurationParameter("com.wire.qa.picklejar.features.package", "com.wire.qa.picklejar.engine.testdata.discovery.cannotfindmethodfeature")
                .selectors(selectPackage("com.wire.qa.picklejar.engine.testdata.discovery.cannotfindmethodfeature"))
                .execute());

        assertThat(exception.getCause().getClass())
                .isEqualTo(JUnitException.class);
        assertThat(exception.getCause().getMessage())
                .isEqualTo("PackageSelector [packageName = 'com.wire.qa.picklejar.engine.testdata.discovery.cannotfindmethodfeature'] resolution failed");
        assertThat(exception.getCause().getCause().getClass())
                .isEqualTo(MethodForStepNotFoundException.class);
        assertThat(exception.getCause().getCause().getMessage())
                .isEqualTo("Could not find match for step 'I cannot find method for step'");
    }

    @Test
    public void multipleMethodsForOneStep() {
        EngineExecutionResults results = EngineTestKit
                .engine("picklejar-engine")
                .enableImplicitConfigurationParameters(false)
                .configurationParameter("com.wire.qa.picklejar.steps.packages", "com.wire.qa.picklejar.engine.testdata.discovery.multiplemethodsforsteps")
                .configurationParameter("com.wire.qa.picklejar.features.package", "com.wire.qa.picklejar.engine.testdata.discovery.multiplemethodsforsteps")
                .selectors(selectPackage(""))
                .execute();

        results.testEvents().assertStatistics(stats -> stats.started(1).succeeded(1));
        results.allEvents().assertStatistics(stats -> stats.reportingEntryPublished(2));
        if (results.allEvents().reportingEntryPublished()
                .stream()
                .anyMatch(e -> e.getPayload().filter(it -> (((ReportEntry) it).getKeyValuePairs().get("method").endsWith("stepActual"))).isPresent())) {
            results.allEvents().reportingEntryPublished().assertEventsMatchExactly(
                    reportEntry("Multiple method step",
                            "com.wire.qa.picklejar.engine.testdata.discovery.multiplemethodsforsteps.MultipleMethodsForSteps.stepActual",
                            "started"),
                    reportEntry("Multiple method step",
                            "com.wire.qa.picklejar.engine.testdata.discovery.multiplemethodsforsteps.MultipleMethodsForSteps.stepActual",
                            "passed")
            );
        } else {
            results.allEvents().reportingEntryPublished().assertEventsMatchExactly(
                    reportEntry("Multiple method step",
                            "com.wire.qa.picklejar.engine.testdata.discovery.multiplemethodsforsteps.MultipleMethodsForSteps.stepDuplication",
                            "started"),
                    reportEntry("Multiple method step",
                            "com.wire.qa.picklejar.engine.testdata.discovery.multiplemethodsforsteps.MultipleMethodsForSteps.stepDuplication",
                            "passed")
            );
        }
    }

    @Test
    public void patternsAreEqual() {
        AnnotationPattern pattern1 = new AnnotationPattern("^123.*");
        AnnotationPattern pattern2 = new AnnotationPattern("^123.*");
        assertThat(pattern1).isEqualTo(pattern2);
    }

    @Test
    public void multipleStepPackages() {
        EngineExecutionResults results = EngineTestKit
                .engine("picklejar-engine")
                .enableImplicitConfigurationParameters(false)
                .configurationParameter("com.wire.qa.picklejar.steps.packages", "com.wire.qa.picklejar.engine.testdata.discovery.multiplesteppackages.package1,com.wire.qa.picklejar.engine.testdata.discovery.multiplesteppackages.package2")
                .configurationParameter("com.wire.qa.picklejar.features.package", "com.wire.qa.picklejar.engine.testdata.discovery.multiplesteppackages")
                .selectors(selectPackage(""))
                .execute();

        results.testEvents().assertStatistics(stats -> stats.started(1).succeeded(1));
        results.allEvents().reportingEntryPublished().assertEventsMatchExactly(
                reportEntry("Multiple method step",
                        "com.wire.qa.picklejar.engine.testdata.discovery.multiplesteppackages.package1.Package1Steps.step1",
                        "started"),
                reportEntry("Multiple method step",
                        "com.wire.qa.picklejar.engine.testdata.discovery.multiplesteppackages.package1.Package1Steps.step1",
                        "passed")
        );
    }

    @Test
    public void unparseableFeatureFile() {
        Exception exception = assertThrows(JUnitException.class, () -> EngineTestKit
                .engine("picklejar-engine")
                .enableImplicitConfigurationParameters(false)
                .configurationParameter("com.wire.qa.picklejar.steps.packages", "com.wire.qa.picklejar.engine.testdata.steps")
                .configurationParameter("com.wire.qa.picklejar.features.package", "com.wire.qa.picklejar.engine.testdata.discovery.unparseable")
                .selectors(selectPackage(""))
                .execute());

        assertThat(exception.getCause().getClass()).isEqualTo(JUnitException.class);
        assertThat(exception.getCause().getMessage()).isEqualTo("PackageSelector [packageName = ''] resolution failed");
        assertThat(exception.getCause().getCause().getClass()).isEqualTo(ParserException.CompositeParserException.class);
        assertThat(exception.getCause().getCause().getMessage()).contains("Parser errors");
    }

    @Test
    public void moreMethodParametersThanStepPlaceholders() {
        Exception exception = assertThrows(JUnitException.class, () -> EngineTestKit
                .engine("picklejar-engine")
                .enableImplicitConfigurationParameters(false)
                .configurationParameter("com.wire.qa.picklejar.steps.packages", "com.wire.qa.picklejar.engine.testdata.discovery.moremethodparameters")
                .configurationParameter("com.wire.qa.picklejar.features.package", "com.wire.qa.picklejar.engine.testdata.discovery.moremethodparameters")
                .selectors(selectPackage(""))
                .filters(TagFilter.includeTags("@moremethodparameters"))
                .execute());

        assertThat(exception.getCause().getClass()).isEqualTo(JUnitException.class);
        assertThat(exception.getCause().getMessage()).isEqualTo("PackageSelector [packageName = ''] resolution failed");
        assertThat(exception.getCause().getCause().getClass()).isEqualTo(MethodForStepNotFoundException.class);
        assertThat(exception.getCause().getCause().getMessage()).contains("Could not find match for step 'Step with x placeholder'");
    }

    @Test
    public void moreStepPlaceholdersThanMethodParameters() {
        Exception exception = assertThrows(JUnitException.class, () -> EngineTestKit
                .engine("picklejar-engine")
                .enableImplicitConfigurationParameters(false)
                .configurationParameter("com.wire.qa.picklejar.steps.packages", "com.wire.qa.picklejar.engine.testdata.discovery.morestepplaceholders")
                .configurationParameter("com.wire.qa.picklejar.features.package", "com.wire.qa.picklejar.engine.testdata.discovery.morestepplaceholders")
                .selectors(selectPackage(""))
                .filters(TagFilter.includeTags("@moremethodparameters"))
                .execute());

        assertThat(exception.getCause().getClass()).isEqualTo(JUnitException.class);
        assertThat(exception.getCause().getMessage()).isEqualTo("PackageSelector [packageName = ''] resolution failed");
        assertThat(exception.getCause().getCause().getClass()).isEqualTo(MethodForStepNotFoundException.class);
        assertThat(exception.getCause().getCause().getMessage()).contains("Could not find match for step 'Step with x placeholder and y placeholder'");
    }

    @Test
    public void castPlaceholders() {
        EngineExecutionResults results = EngineTestKit
                .engine("picklejar-engine")
                .enableImplicitConfigurationParameters(false)
                .configurationParameter("com.wire.qa.picklejar.steps.packages", "com.wire.qa.picklejar.engine.testdata.discovery.castplaceholders")
                .configurationParameter("com.wire.qa.picklejar.features.package", "com.wire.qa.picklejar.engine.testdata.discovery.castplaceholders")
                .selectors(selectPackage(""))
                .execute();

        results.testEvents().assertStatistics(stats -> stats.started(1).succeeded(1));
        results.testEvents().succeeded().assertEventsMatchExactly(
                event(displayName("Cast placeholders"))
        );
    }

    @Test
    public void castFails() {
        Exception exception = assertThrows(JUnitException.class, () -> EngineTestKit
                .engine("picklejar-engine")
                .enableImplicitConfigurationParameters(false)
                .configurationParameter("com.wire.qa.picklejar.steps.packages", "com.wire.qa.picklejar.engine.testdata.discovery.castfails")
                .configurationParameter("com.wire.qa.picklejar.features.package", "com.wire.qa.picklejar.engine.testdata.discovery.castfails")
                .selectors(selectPackage(""))
                .execute()
        );

        String clasz = "int";
        String value = "text";
        assertThat(exception.getCause().getClass()).isEqualTo(JUnitException.class);
        assertThat(exception.getCause().getMessage()).isEqualTo("PackageSelector [packageName = ''] resolution failed");
        assertThat(exception.getCause().getCause().getClass()).isEqualTo(MethodForStepNotFoundException.class);
        assertThat(exception.getCause().getCause().getMessage()).contains(
                String.format("[Discovery] Methods: Step 'Step with text as %s parameter' has wrong parameter type. Expected type: %s. Actual value: %s",
                        clasz, clasz, value));
    }

    @Test
    public void featuresInSubdirectories() {
        EngineExecutionResults results = EngineTestKit
                .engine("picklejar-engine")
                .enableImplicitConfigurationParameters(false)
                .configurationParameter("com.wire.qa.picklejar.steps.packages", "com.wire.qa.picklejar.engine.testdata.discovery.subdirectories")
                .configurationParameter("com.wire.qa.picklejar.features.package", "com.wire.qa.picklejar.engine.testdata.discovery.subdirectories")
                .selectors(selectPackage(""))
                .execute();

        results.testEvents().assertStatistics(stats -> stats.started(2).succeeded(2));
        results.testEvents().succeeded().assertEventsMatchLoosely(
                event(finishedSuccessfully(), displayName("Scenario in Subdirectory")),
                event(finishedSuccessfully(), displayName("Scenario in Parent Directory"))
        );
        results.allEvents().reportingEntryPublished().assertEventsMatchLoosely(
                reportEntry("Step from Subdirectory Scenario", "started"),
                reportEntry("Step from Subdirectory Scenario", "passed"),
                reportEntry("Step from Parent Directory Scenario", "started"),
                reportEntry("Step from Parent Directory Scenario", "passed")
        );
    }

    @Test
    public void duplicateScenarioNameInOneFeatureFile() {
        Exception exception = assertThrows(JUnitException.class, () -> EngineTestKit
                .engine("picklejar-engine")
                .enableImplicitConfigurationParameters(false)
                .configurationParameter("com.wire.qa.picklejar.steps.packages", "com.wire.qa.picklejar.engine.testdata.discovery.duplicatescenarioname")
                .configurationParameter("com.wire.qa.picklejar.features.package", "com.wire.qa.picklejar.engine.testdata.discovery.duplicatescenarioname")
                .selectors(selectPackage(""))
                .execute()
        );

        String feature = "Feature";
        String scenarioName = "Same name";
        assertThat(exception.getCause().getClass()).isEqualTo(JUnitException.class);
        assertThat(exception.getCause().getMessage()).isEqualTo("PackageSelector [packageName = ''] resolution failed");
        assertThat(exception.getCause().getCause().getClass()).isEqualTo(DiscoveryException.class);
        assertThat(exception.getCause().getCause().getMessage()).contains(
                String.format("Duplicate scenario name(s) in feature '%s': %s",
                        feature, scenarioName));
    }

    @Test
    public void duplicateScenarioNameWithSameFeatureNameInDifferentDirectories() {
        EngineExecutionResults results1 = EngineTestKit
                .engine("picklejar-engine")
                .enableImplicitConfigurationParameters(false)
                .configurationParameter("com.wire.qa.picklejar.steps.packages", "com.wire.qa.picklejar.engine.testdata.discovery.samefeaturename")
                .configurationParameter("com.wire.qa.picklejar.features.package", "com.wire.qa.picklejar.engine.testdata.discovery.samefeaturename")
                .selectors(selectPackage(""))
                .filters(TagFilter.includeTags("@InSubdirectory"))
                .execute();
        EngineExecutionResults results2 = EngineTestKit
                .engine("picklejar-engine")
                .enableImplicitConfigurationParameters(false)
                .configurationParameter("com.wire.qa.picklejar.steps.packages", "com.wire.qa.picklejar.engine.testdata.discovery.samefeaturename")
                .configurationParameter("com.wire.qa.picklejar.features.package", "com.wire.qa.picklejar.engine.testdata.discovery.samefeaturename")
                .selectors(selectPackage(""))
                .filters(TagFilter.includeTags("@InParentDirectory"))
                .execute();

        // both scenarios should be found and executed
        results1.testEvents().assertStatistics(stats -> stats.started(1).succeeded(1));
        results2.testEvents().assertStatistics(stats -> stats.started(1).succeeded(1));
    }

    @Test
    public void generateRelativeFeatureFolder() throws IOException {
        Set<File> featuresPackageFolders = new HashSet<>();
        featuresPackageFolders.add(Files.createTempDirectory(null).toFile());
        File featureFileInRoot = Files.createTempFile(featuresPackageFolders.stream().findFirst().get().toPath(), "Account", ".feature")
                .toFile();
        File subdirectory = Files.createTempDirectory(featuresPackageFolders.stream().findFirst().get().toPath(), null).toFile();
        File featureFileInSubdirectory = Files.createTempFile(subdirectory.toPath(), "Account", ".feature")
                .toFile();

        assertThat(FeatureSelectorResolver.getRelativeFeatureFolder(
                featureFileInRoot,
                featuresPackageFolders)).isEqualTo("");
        assertThat(FeatureSelectorResolver.getRelativeFeatureFolder(
                featureFileInSubdirectory,
                featuresPackageFolders)).isEqualTo(subdirectory.getName() + "/");
    }

    @Test
    public void errorOnGenerateRelativeFeatureFolderWithUnrelatedFolder() throws IOException {
        File featuresPackageFolder = Files.createTempDirectory(null).toFile();
        File featureFileInRoot = Files.createTempFile(featuresPackageFolder.toPath(), "Account", ".feature")
                .toFile();
        File subdirectory = Files.createTempDirectory(featuresPackageFolder.toPath(), null).toFile();
        File featureFileInSubdirectory = Files.createTempFile(subdirectory.toPath(), "Account", ".feature")
                .toFile();
        Set<File> onlyUnrelatedPackageFolders = new HashSet<>();
        onlyUnrelatedPackageFolders.add(Files.createTempDirectory(null).toFile());

        Exception exception = assertThrows(RuntimeException.class, () -> FeatureSelectorResolver.getRelativeFeatureFolder(
                featureFileInRoot,
                onlyUnrelatedPackageFolders));
        assertThat(exception.getClass()).isEqualTo(RuntimeException.class);
        assertThat(exception.getMessage()).contains(
                String.format("[Discovery] Could not relate any feature package folders to file ",
                        featureFileInRoot.getAbsolutePath()));
        Exception exception2 = assertThrows(RuntimeException.class, () -> FeatureSelectorResolver.getRelativeFeatureFolder(
                featureFileInSubdirectory,
                onlyUnrelatedPackageFolders));
        assertThat(exception2.getClass()).isEqualTo(RuntimeException.class);
        assertThat(exception2.getMessage()).contains(
                String.format("[Discovery] Could not relate any feature package folders to file ",
                        featureFileInRoot.getAbsolutePath()));
    }
}