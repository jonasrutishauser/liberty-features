package io.github.jonasrutishauser.liberty.micrometer.cdi;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.micrometer.common.annotation.NoOpValueResolver;
import io.micrometer.common.annotation.ValueExpressionResolver;
import io.micrometer.common.annotation.ValueResolver;
import io.micrometer.common.util.StringUtils;
import io.micrometer.core.aop.MeterTag;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import jakarta.inject.Inject;
import jakarta.interceptor.InvocationContext;

public class MeterTagsSupport {

    private final Map<Class<?>, ValueResolver> valueResolvers;
    private final ValueExpressionResolver valueExpressionResolver;

    @Inject
    public MeterTagsSupport(Map<Class<?>, ValueResolver> valueResolvers,
            ValueExpressionResolver valueExpressionResolver) {
        this.valueResolvers = valueResolvers;
        this.valueExpressionResolver = valueExpressionResolver;
    }

    Tags getTags(InvocationContext context) {
        return getCommonTags(context).and(getMeterTags(context));
    }

    private Tags getMeterTags(InvocationContext context) {
        List<Tag> tags = new ArrayList<>();
        Method method = context.getMethod();
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter methodParameter = parameters[i];
            MeterTag annotation = methodParameter.getAnnotation(MeterTag.class);
            if (annotation != null) {
                Object parameterValue = context.getParameters()[i];

                tags.add(Tag.of(resolveTagKey(annotation, methodParameter.getName()),
                        resolveTagValue(annotation, parameterValue)));
            }
        }
        return Tags.of(tags);
    }

    private static Tags getCommonTags(InvocationContext context) {
        Method method = context.getMethod();
        String className = method.getDeclaringClass().getName();
        String methodName = method.getName();
        return Tags.of("class", className, "method", methodName);
    }

    private String resolveTagValue(MeterTag annotation, Object parameterValue) {
        String value = null;
        if (annotation.resolver() != NoOpValueResolver.class) {
            ValueResolver valueResolver = valueResolvers.get(annotation.resolver());
            value = valueResolver.resolve(parameterValue);
        } else if (StringUtils.isNotBlank(annotation.expression())) {
            if (valueExpressionResolver == null) {
                throw new IllegalArgumentException("No valueExpressionResolver is defined");
            }
            value = valueExpressionResolver.resolve(annotation.expression(), parameterValue);
        } else if (parameterValue != null) {
            value = parameterValue.toString();
        }
        return value == null ? "" : value;
    }

    private static String resolveTagKey(MeterTag annotation, String parameterName) {
        if (StringUtils.isNotBlank(annotation.value())) {
            return annotation.value();
        } else if (StringUtils.isNotBlank(annotation.key())) {
            return annotation.key();
        } else {
            return parameterName;
        }
    }

}
