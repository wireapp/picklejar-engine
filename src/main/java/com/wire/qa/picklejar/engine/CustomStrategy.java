package com.wire.qa.picklejar.engine;

import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.support.hierarchical.ParallelExecutionConfiguration;
import org.junit.platform.engine.support.hierarchical.ParallelExecutionConfigurationStrategy;

import java.util.logging.Logger;

/*
This class implements a workaround for https://github.com/junit-team/junit5/issues/1858 described in
https://github.com/SeleniumHQ/selenium/issues/9359#issuecomment-826785222

This workaround works only with JDK 11 (not with JDK 8 and not with JDK 17).
 */
public class CustomStrategy implements ParallelExecutionConfiguration, ParallelExecutionConfigurationStrategy {

    private static final Logger logger = Logger.getLogger(PicklejarEngine.class.getName());
    private static final int KEEP_ALIVE_SECONDS = 30;
    private static int parallelism;

    @Override
    public int getParallelism() {
        return parallelism;
    }

    @Override
    public int getMinimumRunnable() {
        return 0;
    }

    @Override
    public int getMaxPoolSize() {
        return parallelism;
    }

    @Override
    public int getCorePoolSize() {
        return parallelism;
    }

    @Override
    public int getKeepAliveSeconds() {
        return KEEP_ALIVE_SECONDS;
    }

    @Override
    public ParallelExecutionConfiguration createConfiguration(final ConfigurationParameters configurationParameters) {
        parallelism = Integer.parseInt(System.getProperty("picklejar.parallelism"));
        logger.info("[Execution] Using custom strategy for parallelism = " + parallelism);
        return this;
    }
}