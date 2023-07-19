package com.wire.qa.picklejar.engine.annotations;

import java.util.Objects;
import java.util.regex.Pattern;

public class AnnotationPattern {

    private Pattern pattern;
    private String annotation;

    public AnnotationPattern(String annotation) {
        this.annotation = annotation;
        this.pattern = Pattern.compile(annotation);
    }

    public Pattern getPattern() {
        return pattern;
    }

    public String getAnnotation() {
        return annotation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnnotationPattern that = (AnnotationPattern) o;
        return Objects.equals(annotation, that.annotation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(annotation);
    }
}
