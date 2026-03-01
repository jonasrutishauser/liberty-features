package io.micrometer.prometheus;

public class PrometheusMeterRegistry extends io.micrometer.prometheusmetrics.PrometheusMeterRegistry {

    public PrometheusMeterRegistry(PrometheusConfig config) {
        super(config);
    }

}
