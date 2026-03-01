package io.github.jonasrutishauser.liberty.micrometer.cdi;

import io.micrometer.core.annotation.Counted;
import jakarta.enterprise.util.AnnotationLiteral;

final class CountedLiteral extends AnnotationLiteral<Counted> implements Counted {
    @Override
    public String value() {
        return "";
    }

    @Override
    public boolean recordFailuresOnly() {
        return false;
    }

    @Override
    public String[] extraTags() {
        return new String[0];
    }

    @Override
    public String description() {
        return "";
    }
}