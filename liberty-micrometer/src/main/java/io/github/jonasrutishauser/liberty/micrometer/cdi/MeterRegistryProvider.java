package io.github.jonasrutishauser.liberty.micrometer.cdi;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import jakarta.enterprise.inject.Produces;

public class MeterRegistryProvider {

    @Produces
    MeterRegistry getMeterRegistry() {
        return Metrics.globalRegistry;
    }

}
