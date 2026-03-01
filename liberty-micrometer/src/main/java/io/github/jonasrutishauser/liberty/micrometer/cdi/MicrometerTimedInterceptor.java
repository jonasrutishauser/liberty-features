package io.github.jonasrutishauser.liberty.micrometer.cdi;

import static jakarta.interceptor.Interceptor.Priority.LIBRARY_BEFORE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer.Builder;
import io.micrometer.core.instrument.Timer.Sample;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

@Timed
@Interceptor
@Priority(LIBRARY_BEFORE + 10)
public class MicrometerTimedInterceptor {

    private static final Logger LOGGER = Logger.getLogger(MicrometerTimedInterceptor.class.getName());

    private final MeterRegistry registry;
    private final MeterTagsSupport tagsSupport;

    @Inject
    public MicrometerTimedInterceptor(MeterRegistry registry, MeterTagsSupport tagsSupport) {
        this.registry = registry;
        this.tagsSupport = tagsSupport;
    }

    @AroundInvoke
    Object countedMethod(InvocationContext context) throws Exception {
        final List<Timer> timers = getTimers(context);

        if (timers.isEmpty()) {
            return context.proceed();
        }

        if (CompletionStage.class.isAssignableFrom(context.getMethod().getReturnType())) {
            try {
                return ((CompletionStage<?>) context.proceed()).whenComplete(
                        (result, throwable) -> stop(timers, InterceptorHelper.getExceptionTag(throwable)));
            } catch (Throwable t) {
                stop(timers, InterceptorHelper.getExceptionTag(t));
                throw t;
            }
        }

        String exceptionClass = InterceptorHelper.getExceptionTag(null);
        try {
            return context.proceed();
        } catch (Throwable t) {
            exceptionClass = InterceptorHelper.getExceptionTag(t);
            throw t;
        } finally {
            stop(timers, exceptionClass);
        }
    }

    private void stop(List<Timer> timers, String throwableClassName) {
        for (Timer timer : timers) {
            timer.stop(throwableClassName);
        }
    }

    private void record(Timed timed, Sample sample, String exceptionClass, Tags timerTags) {
        final String metricName = timed.value().isEmpty() ? "method.timed" : timed.value();
        try {
            Builder builder = io.micrometer.core.instrument.Timer.builder(metricName)
                    .description(timed.description().isEmpty() ? null : timed.description())
                    .tags(timerTags)
                    .tag("exception", exceptionClass)
                    .publishPercentileHistogram(timed.histogram())
                    .publishPercentiles(timed.percentiles().length == 0 ? null : timed.percentiles());

            sample.stop(builder.register(registry));
        } catch (Exception e) {
            // ignoring on purpose
            LOGGER.log(Level.WARNING, "Unable to record observed timer value for " + metricName
                    + " with exceptionClass " + exceptionClass, e);
        }
    }

    private List<Timer> getTimers(InvocationContext context) {
        Collection<Timed> timed = InterceptorHelper.findInterceptorBindings(context, Timed.class);
        if (timed.isEmpty()) {
            return Collections.emptyList();
        }
        Tags tags = tagsSupport.getTags(context);
        List<Timer> timers = new ArrayList<>(timed.size());
        for (Timed t : timed) {
            if (t.longTask()) {
                timers.add(new LongTimer(t, tags));
            } else {
                timers.add(new NormalTimer(t, tags));
            }
        }
        return timers;
    }

    private abstract static class Timer {
        protected final Timed timed;
        protected final Tags commonTags;

        public Timer(Timed timed, Tags commonTags) {
            this.timed = timed;
            this.commonTags = commonTags;
        }

        protected String metricName() {
            return timed.value().isEmpty() ? "method.timed" : timed.value();
        }

        public abstract void stop(String exceptionClass);
    }

    private final class NormalTimer extends Timer {
        private final Sample sample;

        public NormalTimer(Timed timed, Tags commonTags) {
            super(timed, commonTags);
            this.sample = io.micrometer.core.instrument.Timer.start(registry);
        }

        @Override
        public void stop(String exceptionClass) {
            record(timed, sample, exceptionClass, Tags.concat(commonTags, timed.extraTags()));
        }
    }

    private final class LongTimer extends Timer {
        private final LongTaskTimer.Sample sample;

        public LongTimer(Timed timed, Tags commonTags) {
            super(timed, commonTags);
            this.sample = startLongTaskTimer(timed, commonTags);
        }

        private LongTaskTimer.Sample startLongTaskTimer(Timed timed, Tags commonTags) {
            try {
                return LongTaskTimer.builder(metricName())
                        .description(timed.description().isEmpty() ? null : timed.description())
                        .tags(commonTags)
                        .tags(timed.extraTags())
                        .publishPercentileHistogram(timed.histogram())
                        .register(registry)
                        .start();
            } catch (Exception e) {
                // ignoring on purpose
                LOGGER.log(Level.WARNING, "Unable to create long task timer named " + metricName(), e);
                return null;
            }
        }

        @Override
        public void stop(String exceptionClass) {
            try {
                sample.stop();
            } catch (Exception e) {
                // ignoring on purpose
                LOGGER.log(Level.WARNING , "Unable to update long task timer named " + metricName(), e);
            }
        }
    }
}
