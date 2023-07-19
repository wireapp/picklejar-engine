package com.wire.qa.picklejar.engine.discovery;

import java.io.File;
import java.io.FilenameFilter;

public class FeatureFileFilter implements FilenameFilter {

    public static final String EXTENSION = "feature";

    public boolean accept(File dir, String name) {
        return (name.endsWith(EXTENSION));
    }
}
