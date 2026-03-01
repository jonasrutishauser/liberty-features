package io.github.jonasrutishauser.liberty.features.test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.aop.MeterTag;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.Startup;
import jakarta.inject.Inject;

@ApplicationScoped
public class StartupBean {

    private final MeterRegistry registry;
    private final List<String> list = new CopyOnWriteArrayList<>();

    @Inject
    StartupBean(MeterRegistry registry) {
        this.registry = registry;
    }

    @Counted(value = "startup.count", extraTags = {"type", "startup"}, description = "Counts the number of startups")
    @Timed(value = "startup.time", extraTags = {"type", "startup"})
    void startup(@Observes @MeterTag(resolver = ClassNameValueResolver.class) Startup event) {
        System.out.println("StartupBean started");
        registerMeters();
        printMeters();
    }

    private void registerMeters() {
        registry.gaugeCollectionSize("sample.list", Tags.of("key", "value"), list);
        Counter.builder("count.me").baseUnit("beans").description("a description").tags("region", "test")
                .register(registry).increment();
    }

    private void printMeters() {
        System.out.println(registry.getMeters());
    }

}
