package com.wire.qa.picklejar.engine.tests;

import com.wire.qa.picklejar.engine.discovery.FeatureSelectorResolver;
import com.wire.qa.picklejar.engine.exception.DiscoveryException;
import gherkin.ast.Feature;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.testkit.engine.EngineTestKit;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

/**
 * The following characters might influence test rerun/deflake: ; , + #
 * <p>
 * The following characters might influence unique id of JUnit 5: [ ] :
 * <p>
 * Not tested or illegal: ( )
 */
public class NamingTests {

    @Test
    public void featureNameWithIllegalCharacters() {
        String[] illegalCharacters = {";", "+", "#", ":", "[", "]", ",", "\\", "\""};
        StringBuilder name = new StringBuilder("Feature with illegal characters ");
        for (String illegalCharacter : illegalCharacters) {
            name.append(illegalCharacter);
        }

        Feature feature = new Feature(new ArrayList<>(), null, null, null, name.toString(), null, null, new ArrayList<>(), new ArrayList<>());
        Feature actual = FeatureSelectorResolver.normalizeName(feature);
        assertThat(actual.getName()).isEqualTo("Feature with illegal characters");
    }

    @Test
    public void scenarioNameWithPlus() {
        Exception exception = assertThrows(JUnitException.class, () -> EngineTestKit
                .engine("picklejar-engine")
                .enableImplicitConfigurationParameters(false)
                .configurationParameter("com.wire.qa.picklejar.steps.packages", "com.wire.qa.picklejar.engine.testdata.steps")
                .configurationParameter("com.wire.qa.picklejar.features.package", "com.wire.qa.picklejar.engine.testdata.naming.scenarioname")
                .selectors(selectPackage(""))
                .execute()
        );

        String illegalCharacter = "+";
        assertThat(exception.getCause().getClass()).isEqualTo(JUnitException.class);
        assertThat(exception.getCause().getMessage()).isEqualTo("PackageSelector [packageName = ''] resolution failed");
        assertThat(exception.getCause().getCause().getClass()).isEqualTo(DiscoveryException.class);
        assertThat(exception.getCause().getCause().getMessage()).contains(
                String.format("[Discovery] Scenario 'Name with %s' contains illegal character '%s' in name in feature '%s'",
                        illegalCharacter, illegalCharacter, "Featurename"));
    }

}
