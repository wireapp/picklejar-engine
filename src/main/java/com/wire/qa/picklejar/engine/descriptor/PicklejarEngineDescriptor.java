package com.wire.qa.picklejar.engine.descriptor;

import com.wire.qa.picklejar.engine.PicklejarConfiguration;
import com.wire.qa.picklejar.engine.PicklejarEngineExecutionContext;

import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.engine.support.hierarchical.Node;

public class PicklejarEngineDescriptor extends EngineDescriptor implements Node<PicklejarEngineExecutionContext> {

    public static final String ENGINE_ID = "picklejar-engine";

    private final PicklejarConfiguration configuration;

    public PicklejarEngineDescriptor(UniqueId uniqueId, PicklejarConfiguration configuration) {
        super(uniqueId, "Picklejar Engine");
        this.configuration = configuration;
    }

    public PicklejarConfiguration getConfiguration() {
        return configuration;
    }

}