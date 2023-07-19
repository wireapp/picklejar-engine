package com.wire.qa.picklejar.engine.tests;

import com.wire.qa.picklejar.launcher.TestPropertyParser;
import com.wire.qa.picklejar.launcher.UniqueIdFilter;
import org.junit.jupiter.api.Test;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

public class LauncherTests {

    @Test
    public void parseTestParameter() {
        List<String> result = TestPropertyParser.parse("Feature1#Scenario1+Scenario2,Feature2#Scenario1+Scenario2 1+Scenario2 2");
        assertThat(result).containsExactly("[engine:picklejar-engine]/[Feature:Feature1]/[Scenario:Scenario1]",
                "[engine:picklejar-engine]/[Feature:Feature1]/[Scenario:Scenario2]",
                "[engine:picklejar-engine]/[Feature:Feature2]/[Scenario:Scenario1]",
                "[engine:picklejar-engine]/[Feature:Feature2]/[Scenario:Scenario2]/[Example:1]",
                "[engine:picklejar-engine]/[Feature:Feature2]/[Scenario:Scenario2]/[Example:2]");
    }

    @Test
    public void parseTestParameterContainsScenarioWithComma() {
        List<String> result = TestPropertyParser.parse("Feature1#Scenario1 with , comma+Scenario2,Feature2#Scenario1+Scenario2 1+Scenario2 2");
        assertThat(result).containsExactly("[engine:picklejar-engine]/[Feature:Feature1]/[Scenario:Scenario1 with , comma]",
                "[engine:picklejar-engine]/[Feature:Feature1]/[Scenario:Scenario2]",
                "[engine:picklejar-engine]/[Feature:Feature2]/[Scenario:Scenario1]",
                "[engine:picklejar-engine]/[Feature:Feature2]/[Scenario:Scenario2]/[Example:1]",
                "[engine:picklejar-engine]/[Feature:Feature2]/[Scenario:Scenario2]/[Example:2]");
    }

    @Test
    public void parseTestParameterWithForbiddenCharactersForUniqueIds() {
        List<String> uniqueIds = TestPropertyParser.parse("Feature1#Scenario with : in name 0+Scenario with / in name,Feature2#Scenario with [ in name+Scenario with ] in name 0");
        assertThat(uniqueIds).containsExactly(
                "[engine:picklejar-engine]/[Feature:Feature1]/[Scenario:Scenario with %3A in name]/[Example:0]",
                "[engine:picklejar-engine]/[Feature:Feature1]/[Scenario:Scenario with %2F in name]",
                "[engine:picklejar-engine]/[Feature:Feature2]/[Scenario:Scenario with %5B in name]",
                "[engine:picklejar-engine]/[Feature:Feature2]/[Scenario:Scenario with %5D in name]/[Example:0]");
        EngineExecutionResults results = EngineTestKit
                .engine("picklejar-engine")
                .enableImplicitConfigurationParameters(false)
                .configurationParameter("com.wire.qa.picklejar.steps.packages", "com.wire.qa.picklejar.engine.testdata.launcher")
                .configurationParameter("com.wire.qa.picklejar.features.package", "com.wire.qa.picklejar.engine.testdata.launcher")
                .selectors(selectPackage(""))
                .filters(UniqueIdFilter.includeIds(uniqueIds))
                .execute();

        results.testEvents().assertStatistics(stats -> stats.started(4).succeeded(4));
    }

    @Test
    public void parseTestParameterWithSubdirectories() {
        List<String> result = TestPropertyParser.parse("Subdirectory1/Feature1#Scenario1+Scenario2,Feature2#Scenario1,Subdirectory3/Feature3#Scenario1 1");
        assertThat(result).containsExactly("[engine:picklejar-engine]/[Feature:Subdirectory1%2FFeature1]/[Scenario:Scenario1]",
                "[engine:picklejar-engine]/[Feature:Subdirectory1%2FFeature1]/[Scenario:Scenario2]",
                "[engine:picklejar-engine]/[Feature:Feature2]/[Scenario:Scenario1]",
                "[engine:picklejar-engine]/[Feature:Subdirectory3%2FFeature3]/[Scenario:Scenario1]/[Example:1]");
    }

    @Test
    public void parseTestParameterWithSubdirectoriesAndAllowedSpecialCharacters() {
        List<String> result = TestPropertyParser.parse("Subdirectory-with-dash/Feature#Scenario");
        assertThat(result).containsExactly("[engine:picklejar-engine]/[Feature:Subdirectory-with-dash%2FFeature]/[Scenario:Scenario]");
    }

}
