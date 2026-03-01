package io.github.jonasrutishauser.liberty.micrometer.cdi;

import static jakarta.interceptor.Interceptor.Priority.LIBRARY_BEFORE;

import java.lang.reflect.Method;
import java.util.concurrent.CompletionStage;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

@Interceptor
@Priority(LIBRARY_BEFORE + 10)
public class MicrometerCountedInterceptor {

    private final MeterRegistry registry;
    private final MeterTagsSupport tagsSupport;

    @Inject
    public MicrometerCountedInterceptor(MeterRegistry registry, MeterTagsSupport tagsSupport) {
        this.registry = registry;
        this.tagsSupport = tagsSupport;
    }

    @AroundInvoke
    Object countedMethod(InvocationContext context) throws Exception {
        Counted counted = InterceptorHelper.findInterceptorBinding(context, Counted.class);
        if (counted == null) {
            return context.proceed();
        }
        Method method = context.getMethod();
        Tags tags = tagsSupport.getTags(context);

        Class<?> returnType = method.getReturnType();
        if (CompletionStage.class.isAssignableFrom(returnType)) {
            try {
                return ((CompletionStage<?>) context.proceed())
                        .whenComplete((result, throwable) -> recordCompletionResult(counted, tags, throwable));
            } catch (Throwable throwable) {
                recordResult(counted, tags, throwable);
                throw throwable;
            }
        }

        try {
            Object result = context.proceed();
            if (!counted.recordFailuresOnly()) {
                recordResult(counted, tags, null);
            }
            return result;
        } catch (Throwable e) {
            recordResult(counted, tags, e);
            throw e;
        }
    }

    private void recordCompletionResult(Counted counted, Tags commonTags, Throwable throwable) {
        if (throwable != null) {
            recordResult(counted, commonTags, throwable);
        } else if (!counted.recordFailuresOnly()) {
            recordResult(counted, commonTags, null);
        }
    }

    private void recordResult(Counted counted, Tags commonTags, Throwable throwable) {
        Counter.Builder builder = Counter.builder(counted.value()).tags(commonTags).tags(counted.extraTags())
                .tag("exception", InterceptorHelper.getExceptionTag(throwable)).tag("result", throwable == null ? "success" : "failure");
        String description = counted.description();
        if (!description.isEmpty()) {
            builder.description(description);
        }
        builder.register(registry).increment();
    }

}
