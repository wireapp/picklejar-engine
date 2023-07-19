package com.wire.qa.picklejar.engine.descriptor;

import com.wire.qa.picklejar.engine.PicklejarEngineExecutionContext;
import com.wire.qa.picklejar.engine.gherkin.model.Feature;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.hierarchical.Node;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class FeatureDescriptor extends AbstractTestDescriptor implements Node<PicklejarEngineExecutionContext> {

    private Feature feature;
    private Set<TestTag> tags = new HashSet<>();
    private File file;

    public FeatureDescriptor(UniqueId uniqueId, String featureName, File file, String featureFolder) {
        super(uniqueId.append("Feature", featureFolder + featureName),
                featureFolder + featureName);
        this.file = file;
    }

    @Override
    public Set<TestTag> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags.stream().map(TestTag::create).collect(Collectors.toSet());
    }

    @Override
    public Type getType() {
        return Type.CONTAINER;
    }

    public Feature getFeature() {
        if (feature == null) {
            feature = new Feature(this.getDisplayName());
        }
        return feature;
    }

    public File getFile() {
        return file;
    }
}
