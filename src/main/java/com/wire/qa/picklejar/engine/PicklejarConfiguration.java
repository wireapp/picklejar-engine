package com.wire.qa.picklejar.engine;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.wire.qa.picklejar.engine.discovery.ResourceSeeker;
import com.wire.qa.picklejar.engine.exception.MisconfigurationException;

import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.ConfigurationParameters;

public class PicklejarConfiguration {

    private static final String PARALLEL_EXECUTION_ENABLED_PROPERTY_NAME = "junit.jupiter.execution.parallel.enabled";
    private static final String FEATURE_FILE_PACKAGE_PROPERTY_NAME = "com.wire.qa.picklejar.features.package";
    private static final String STEPS_PACKAGES_PROPERTY_NAME = "com.wire.qa.picklejar.steps.packages";
    private static final String XML_REPORTS_DIRECTORY_PROPERTY_NAME = "com.wire.qa.picklejar.xml-reports.directory";
    private static final String CUCUMBER_REPORT_FILENAME_PROPERTY_NAME = "com.wire.qa.picklejar.cucumber-report.filename";
    private static final String LOGGING_FORMAT = "com.wire.qa.picklejar.engine.logging.format";
    private static final String MULTIPLE_STEPS_MATCHING_WARNING = "com.wire.qa.picklejar.engine.multiple-steps-matching-warning";

    private final ConfigurationParameters configurationParameters;

    public PicklejarConfiguration(ConfigurationParameters configurationParameters) {
        this.configurationParameters = Preconditions.notNull(configurationParameters,
                "ConfigurationParameters must not be null");
    }

    public Optional<String> getRawConfigurationParameter(String key) {
        return configurationParameters.get(key);
    }

    public boolean isParallelExecutionEnabled() {
        return configurationParameters.getBoolean(PARALLEL_EXECUTION_ENABLED_PROPERTY_NAME).orElse(false);
    }

    public String getFeaturesPackageName() {
        return configurationParameters.get(FEATURE_FILE_PACKAGE_PROPERTY_NAME)
                .orElseThrow(() -> new MisconfigurationException(String.format(
                        "Please add property %s with package name of resources which contain feature files",
                        FEATURE_FILE_PACKAGE_PROPERTY_NAME)));
    }

    public Set<File> getFeaturesPackagePaths() {
        return ResourceSeeker.getResourceDirectoriesFromPackage(getFeaturesPackageName());
    }

    public List<String> getStepsPackageNames() {
        String names = configurationParameters.get(STEPS_PACKAGES_PROPERTY_NAME)
                .orElseThrow(() -> new MisconfigurationException(String.format(
                        "Please add property %s with package name containing steps classes (if multiple separate by comma)",
                        STEPS_PACKAGES_PROPERTY_NAME)));
        return Arrays.stream(names.split(",")).collect(Collectors.toList());
    }

    public String getXmlReportsDirectory() {
        return configurationParameters.get(XML_REPORTS_DIRECTORY_PROPERTY_NAME).orElse("xml-reports");
    }

    public String getCucumberReportFileName() {
        return configurationParameters.get(CUCUMBER_REPORT_FILENAME_PROPERTY_NAME).orElse("cucumber-report.json");
    }

    public String getLoggingFormat() {
        return configurationParameters.get(LOGGING_FORMAT).orElse("%1$tT %2$s %4$s%n");
    }

    public boolean doMultipleStepsMatchingWarning() {
        return configurationParameters.getBoolean(MULTIPLE_STEPS_MATCHING_WARNING).orElse(true);
    }

}
