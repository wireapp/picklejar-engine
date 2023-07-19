package com.wire.qa.picklejar.engine.descriptor;

import java.util.Optional;

import com.wire.qa.picklejar.engine.gherkin.model.Step;
import org.junit.platform.engine.TestDescriptor;

public class StepDescriptor {

    private TestDescriptor parent;
    private String keyword;
    private String displayName;
    private MethodDescriptor methodDescriptor;
    private Step step = null;

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getKeyword() {
        return this.keyword;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public void setMethodDescriptor(MethodDescriptor methodDescriptor) {
        this.methodDescriptor = methodDescriptor;
    }

    public MethodDescriptor getMethodDescriptor() {
        return this.methodDescriptor;
    }

    public final Optional<TestDescriptor> getParent() {
        return Optional.ofNullable(this.parent);
    }

    public final void setParent(TestDescriptor parent) {
        this.parent = parent;
    }

    public final Step getStep() {
        if (step == null) {
            step = new Step(getKeyword(), getDisplayName());
        }
        return step;
    }
}