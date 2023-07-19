package com.wire.qa.picklejar.engine.tests;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.platform.testkit.engine.EngineTestKit;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

public class PerformanceTests {

    /*
     * Enable following test to test performance of discovery
     */
    @Test @Disabled
    public void discoveryPerformance() {
        EngineTestKit.Builder builer = EngineTestKit
                .engine("picklejar-engine")
                .enableImplicitConfigurationParameters(false)
                .configurationParameter("com.wire.qa.picklejar.steps.packages", "com.wire.qa.picklejar.engine.testdata.performance")
                .configurationParameter("com.wire.qa.picklejar.features.package", "com.wire.qa.picklejar.engine.testdata.performance.discovery")
                .selectors(selectPackage(""));

        int amount = 100;
        long start = System.currentTimeMillis();
        for (int ii = 0 ; ii < amount ; ii++) {
            builer.execute();
        }
        long elapsed = System.currentTimeMillis() - start;
        System.out.printf("elapsed time = %s ms (%s s)%n", elapsed, elapsed / 1000);
        System.out.printf("%s ms per execution%n", elapsed / amount);
    }
}
