package com.wire.qa.picklejar.engine.exception;

import java.io.File;

public class MethodForStepNotFoundException extends DiscoveryException {

    public MethodForStepNotFoundException(String message, File file, int lineNumber) {
        super(message, file, lineNumber);
    }

}
