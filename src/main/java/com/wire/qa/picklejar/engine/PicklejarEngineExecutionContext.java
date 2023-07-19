package com.wire.qa.picklejar.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.wire.qa.picklejar.launcher.listeners.CucumberReportGeneratingListener;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.support.hierarchical.EngineExecutionContext;
import org.junit.platform.engine.support.hierarchical.ThrowableCollector;

public class PicklejarEngineExecutionContext implements EngineExecutionContext {

    private final State state;
    private final Predicate<Class<?>> allTypes = type -> true;
    private final Predicate<String> allNames = name -> true;

    public PicklejarEngineExecutionContext(EngineExecutionListener executionListener,
                                           CucumberReportGeneratingListener cucumberReportGeneratingListener,
                                           PicklejarConfiguration configuration) {
        this(new State(executionListener, cucumberReportGeneratingListener, configuration));
    }

    private PicklejarEngineExecutionContext(State state) {
        this.state = state;
    }

    public EngineExecutionListener getExecutionListener() {
        return this.state.executionListener;
    }

    public CucumberReportGeneratingListener getCucumberReportGeneratingListener() {
        return this.state.cucumberReportGeneratingListener;
    }

    private static final class State implements Cloneable {

        final EngineExecutionListener executionListener;
        final CucumberReportGeneratingListener cucumberReportGeneratingListener;
        final PicklejarConfiguration configuration;
        /*
        TestInstancesProvider testInstancesProvider;
        ExtensionRegistry extensionRegistry;
        ExtensionContext extensionContext;
        */
        ThrowableCollector throwableCollector;

        State(EngineExecutionListener executionListener,
              CucumberReportGeneratingListener cucumberReportGeneratingListener,
              PicklejarConfiguration configuration) {
            this.executionListener = executionListener;
            this.cucumberReportGeneratingListener = cucumberReportGeneratingListener;
            this.configuration = configuration;
        }

        @Override
        public State clone() {
            try {
                return (State) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new JUnitException("State could not be cloned", e);
            }
        }

    }

    public Collection<Class<?>> getTestClassesFromFirstStepsPackage() {
        List<Class<?>> classes = new ArrayList<>();
        classes.addAll(ReflectionUtils.findAllClassesInPackage(
                state.configuration.getStepsPackageNames().get(0),
                allTypes,
                allNames));
        return classes;
    }
}
