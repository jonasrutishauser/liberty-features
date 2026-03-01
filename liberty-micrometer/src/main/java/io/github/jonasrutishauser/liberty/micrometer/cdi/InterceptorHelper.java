package io.github.jonasrutishauser.liberty.micrometer.cdi;

import java.lang.annotation.Annotation;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import jakarta.interceptor.InvocationContext;

public class InterceptorHelper {

    public static <T extends Annotation> T findInterceptorBinding(InvocationContext context, Class<T> annotationType) {
        return annotationType.cast(findInterceptorBinding.apply(context, annotationType));
    }

    public static <T extends Annotation> Collection<T> findInterceptorBindings(InvocationContext context,
            Class<T> annotationType) {
        return findInterceptorBindings.apply(context, annotationType).stream().map(annotationType::cast)
                .collect(Collectors.toList());
    }

    public static String getExceptionTag(Throwable throwable) {
        if (throwable == null) {
            return "none";
        }
        if (throwable.getCause() == null) {
            return throwable.getClass().getSimpleName();
        }
        return throwable.getCause().getClass().getSimpleName();
    }

    private static final BiFunction<InvocationContext, Class<? extends Annotation>, Annotation> findInterceptorBinding = getFindInterceptorBinding();

    private static final BiFunction<InvocationContext, Class<? extends Annotation>, Set<Annotation>> findInterceptorBindings = getFindInterceptorBindings();

    private static BiFunction<InvocationContext, Class<? extends Annotation>, Annotation> getFindInterceptorBinding() {
        try {
            Lookup lookup = MethodHandles.lookup();
            MethodHandle getInterceptorBinding = lookup
                    .unreflect(InvocationContext.class.getMethod("getInterceptorBinding", Class.class));
            return (BiFunction<InvocationContext, Class<? extends Annotation>, Annotation>) LambdaMetafactory.metafactory(lookup, "apply", MethodType.methodType(BiFunction.class),
                    MethodType.methodType(Annotation.class, InvocationContext.class, Class.class),
                    getInterceptorBinding, getInterceptorBinding.type()).getTarget().invokeExact();
        } catch (Throwable e) {
            return InterceptorHelper::findAnnotation;
        }
    }

    private static BiFunction<InvocationContext, Class<? extends Annotation>, Set<Annotation>> getFindInterceptorBindings() {
        try {
            Lookup lookup = MethodHandles.lookup();
            MethodHandle getInterceptorBindings = lookup
                    .unreflect(InvocationContext.class.getMethod("getInterceptorBindings", Class.class));
            return (BiFunction<InvocationContext, Class<? extends Annotation>, Set<Annotation>>) LambdaMetafactory.metafactory(lookup, "apply", MethodType.methodType(BiFunction.class),
                    MethodType.methodType(Set.class, InvocationContext.class, Class.class),
                    getInterceptorBindings, getInterceptorBindings.type()).getTarget().invokeExact();
        } catch (Throwable e) {
            return InterceptorHelper::findAnnotations;
        }
    }

    private static Annotation findAnnotation(InvocationContext context, Class<? extends Annotation> annotationType) {
        return context.getMethod().getAnnotation(annotationType);
    }

    private static Set<Annotation> findAnnotations(InvocationContext context, Class<? extends Annotation> annotationType) {
        return Set.copyOf(Arrays.asList(context.getMethod().getAnnotationsByType(annotationType)));
    }

}
