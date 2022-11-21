package com.salesmanager.shop.store.controller.product;

import org.springframework.stereotype.Component;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;

@Component
public class MetricUtil {


    private final LongCounter counter;

    public MetricUtil() {
        Meter meter = GlobalOpenTelemetry
                .getMeterProvider()
                .meterBuilder(MetricUtil.class.getName())
                .setInstrumentationVersion("1.0.0")
                .build();

        this.counter = meter
                .counterBuilder("ref_counter")
                .setDescription("An example counter for our app")
                .setUnit("1")
                .build();
    }

    public LongCounter referenceCounter() {
        return counter;
    }
}