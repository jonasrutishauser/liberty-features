package io.micrometer.prometheus;

public interface PrometheusConfig extends io.micrometer.prometheusmetrics.PrometheusConfig {

    /**
     * Accept configuration defaults
     */
    PrometheusConfig DEFAULT = k -> null;

}
