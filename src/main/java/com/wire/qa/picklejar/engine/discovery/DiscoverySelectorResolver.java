package com.wire.qa.picklejar.engine.discovery;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

import com.wire.qa.picklejar.engine.descriptor.PicklejarEngineDescriptor;
import org.junit.platform.engine.DiscoveryFilter;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.support.discovery.EngineDiscoveryRequestResolver;

public class DiscoverySelectorResolver {

    private static final Logger logger = Logger.getLogger(DiscoverySelectorResolver.class.getName());

    private MethodCache methodCache = null;

    public DiscoverySelectorResolver() {

    }

    private final EngineDiscoveryRequestResolver<PicklejarEngineDescriptor> resolver = EngineDiscoveryRequestResolver.<PicklejarEngineDescriptor>builder()
            .addSelectorResolver(context -> new FeatureSelectorResolver(context.getEngineDescriptor().getUniqueId(), context.getEngineDescriptor().getConfiguration(), methodCache.getCache()))
            .addSelectorResolver(context -> new ScenarioSelectorResolver(context.getEngineDescriptor().getUniqueId(), context.getEngineDescriptor().getConfiguration(), methodCache.getCache()))
            .addTestDescriptorVisitor(context -> TestDescriptor::prune)
            .build();

    public void discover(EngineDiscoveryRequest request, PicklejarEngineDescriptor engineDescriptor) {
        // https://github.com/jlink/jqwik/blob/master/engine/src/main/java/net/jqwik/engine/discovery/HierarchicalJavaResolver.java
        // java -Dcom.wire.qa.picklejar.steps.packages="com.wire.qa.challenge.webapp" -jar ~/Downloads/junit-platform-console-standalone-1.5.2.jar -e picklejar-engine --class-path=target/test-classes/:target/classes/:picklejar-engine/target/classes/ -cp /Users/sven/.m2/repository/io/cucumber/gherkin3/3.1.0/gherkin3-3.1.0.jar -cp /Users/sven/.m2/repository/info/cukes/gherkin/2.12.2/gherkin-2.12.2.jar -cp /Users/sven/.m2/repository/org/seleniumhq/selenium/selenium-remote-driver/3.11.0/selenium-remote-driver-3.11.0.jar -cp /Users/sven/.m2/repository/org/seleniumhq/selenium/selenium-api/3.11.0/selenium-api-3.11.0.jar -cp /Users/sven/.m2/repository/org/seleniumhq/selenium/selenium-support/3.11.0/selenium-support-3.11.0.jar -cp /Users/sven/.m2/repository/com/squareup/okhttp3/okhttp/3.9.1/okhttp-3.9.1.jar -cp /Users/sven/.m2/repository/com/squareup/okio/okio/1.13.0/okio-1.13.0.jar -cp /Users/sven/.m2/repository/com/google/guava/guava/23.6-jre/guava-23.6-jre.jar -cp /Users/sven/.m2/repository/com/google/code/gson/gson/2.8.2/gson-2.8.2.jar -p com.wire.qa.web --reports-dir=target/junit-standalone
        for (DiscoverySelector selector : request.getSelectorsByType(DiscoverySelector.class)) {
            logger.fine(() -> "[Discovery] Using Selector: " + selector.toString());
        }
        for (DiscoveryFilter filter : request.getFiltersByType(DiscoveryFilter.class)) {
            logger.fine(() -> "[Discovery] Using (deprecated) Filter: " + filter.toString());
        }
        try {
            methodCache = new MethodCache(engineDescriptor.getConfiguration().getStepsPackageNames());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        resolver.resolve(request, engineDescriptor);
    }


}
