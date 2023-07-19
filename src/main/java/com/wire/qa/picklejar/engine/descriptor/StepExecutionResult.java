package com.wire.qa.picklejar.engine.descriptor;

public enum StepExecutionResult {
    STARTED("started"),
    SUCCESSFUL("successful"),
    FAILED("failed");

    private final String name;

    public String getName() {
        return this.name;
    }

    StepExecutionResult(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.getName();
    }
}
