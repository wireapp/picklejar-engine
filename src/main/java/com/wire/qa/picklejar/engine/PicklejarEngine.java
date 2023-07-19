package com.wire.qa.picklejar.engine;

import java.util.Optional;
import java.util.logging.Logger;

import com.wire.qa.picklejar.engine.discovery.DiscoverySelectorResolver;
import com.wire.qa.picklejar.engine.descriptor.PicklejarEngineDescriptor;

import com.wire.qa.picklejar.launcher.listeners.CucumberReportGeneratingListener;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.support.config.PrefixedConfigurationParameters;
import org.junit.platform.engine.support.hierarchical.ForkJoinPoolHierarchicalTestExecutorService;
import org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine;
import org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutorService;

import static com.wire.qa.picklejar.engine.Constants.PARALLEL_CONFIG_PREFIX;
import static com.wire.qa.picklejar.engine.Constants.PARALLEL_EXECUTION_ENABLED_PROPERTY_NAME;

public final class PicklejarEngine extends HierarchicalTestEngine<PicklejarEngineExecutionContext> {

    private static final Logger logger = Logger.getLogger(PicklejarEngine.class.getName());
    private final CucumberReportGeneratingListener cucumberReportGeneratingListener;

    // For tests only
    public PicklejarEngine() {
        cucumberReportGeneratingListener = null;
    }

    public PicklejarEngine(CucumberReportGeneratingListener cucumberReportGeneratingListener) {
        this.cucumberReportGeneratingListener = cucumberReportGeneratingListener;
    }

    @Override
    public Optional<String> getGroupId() {
        return Optional.of("com.wire.qa.picklejar.engine");
    }

    @Override
    public Optional<String> getArtifactId() {
        return Optional.of("picklejar-engine");
    }

    @Override
    public String getId() {
        return PicklejarEngineDescriptor.ENGINE_ID;
    }

    @Override
    public TestDescriptor discover(EngineDiscoveryRequest engineDiscoveryRequest, UniqueId uniqueId) {
        PicklejarConfiguration configuration = new PicklejarConfiguration(engineDiscoveryRequest.getConfigurationParameters());
        PicklejarLogger.configureLogging(configuration);
        if (engineDiscoveryRequest.getConfigurationParameters().size() == 0
                && engineDiscoveryRequest.getSelectorsByType(ClassSelector.class).size() > 0) {
            logger.warning("No configuration given or discovery was started by surefire");
            logger.info("If you see this warning only then you forgot to provide the mandatory properties com.wire.qa.picklejar.features.package and com.wire.qa.picklejar.steps.packages");
            return new PicklejarEngineDescriptor(uniqueId, configuration);
        }
        logger.info("[Discovery] Started");
        PicklejarEngineDescriptor engineDescriptor = new PicklejarEngineDescriptor(uniqueId, configuration);
        new DiscoverySelectorResolver().discover(engineDiscoveryRequest, engineDescriptor);
        return engineDescriptor;
    }

    @Override
    protected HierarchicalTestExecutorService createExecutorService(ExecutionRequest request) {
        ConfigurationParameters config = request.getConfigurationParameters();
        if (config.getBoolean(PARALLEL_EXECUTION_ENABLED_PROPERTY_NAME).orElse(false)) {
            return new ForkJoinPoolHierarchicalTestExecutorService(
                    new PrefixedConfigurationParameters(config, PARALLEL_CONFIG_PREFIX));
        }
        return super.createExecutorService(request);
    }

    @Override
    protected PicklejarEngineExecutionContext createExecutionContext(ExecutionRequest executionRequest) {
        return new PicklejarEngineExecutionContext(executionRequest.getEngineExecutionListener(),
                cucumberReportGeneratingListener,
                getPicklejarConfiguration(executionRequest));
    }

    private PicklejarConfiguration getPicklejarConfiguration(ExecutionRequest request) {
        PicklejarEngineDescriptor engineDescriptor = (PicklejarEngineDescriptor) request.getRootTestDescriptor();
        return engineDescriptor.getConfiguration();
    }

}