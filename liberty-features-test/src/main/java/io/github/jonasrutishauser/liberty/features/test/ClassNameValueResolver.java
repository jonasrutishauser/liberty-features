package io.github.jonasrutishauser.liberty.features.test;

import io.micrometer.common.annotation.ValueResolver;
import jakarta.enterprise.context.Dependent;

@Dependent
public class ClassNameValueResolver implements ValueResolver {
    @Override
    public String resolve(Object value) {
        if (value == null) {
            return "none";
        }
        return value.getClass().getSimpleName();
    }
}