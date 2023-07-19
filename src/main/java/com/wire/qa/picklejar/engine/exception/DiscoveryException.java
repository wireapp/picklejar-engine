package com.wire.qa.picklejar.engine.exception;

import java.io.File;
import java.util.Arrays;

public class DiscoveryException extends RuntimeException {

    public DiscoveryException(String message, File file, int lineNumber) {
        super(message);
        StackTraceElement[] trace = new StackTraceElement[]{
                new StackTraceElement(file.getParent() + File.separator,
                        file.getName(),
                        file.getName(),
                        lineNumber)
        };
        StackTraceElement[] result = Arrays.copyOf(trace, trace.length + this.getStackTrace().length);
        System.arraycopy(this.getStackTrace(), 0, result, trace.length, this.getStackTrace().length);
        this.setStackTrace(result);
    }

}
