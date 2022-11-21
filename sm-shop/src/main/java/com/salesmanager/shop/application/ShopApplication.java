package com.salesmanager.shop.application;

import java.time.Duration;
import java.net.MalformedURLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import com.dynatrace.opentelemetry.metric.DynatraceMetricExporter;

import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class ShopApplication extends SpringBootServletInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShopApplication.class);

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(ShopApplication.class);
    }

    public static void main(String[] args) {
        setupOpenTelemetry();
        SpringApplication.run(ShopApplication.class, args);
    }

    private static void setupOpenTelemetry() {
        // Initialize the Dynatrace metric exporter (via OneAgent)
        DynatraceMetricExporter exporter = null;
        String endpoint = "http://localhost:14499/metrics/ingest";
        try {
            exporter = DynatraceMetricExporter
                    .builder()
                    .setUrl(endpoint)
                    .setPrefix("custom.opentelemetry")
                    .setEnrichWithOneAgentMetaData(true)
                    .build();
        } catch (MalformedURLException e) {
            LOGGER.warn(String.format("Endpoint '%s' is not a valid URL", endpoint));
        }

        // Create the MeterProvider and register it globally.
        // The MeterProvider is configured with the PeriodicMetricReader
        // which takes our exporter and the export interval.
        SdkMeterProvider meterProvider = SdkMeterProvider.builder()
                .registerMetricReader(
                        PeriodicMetricReader.builder(exporter).setInterval(Duration.ofSeconds(60)).build())
                .build();

        // Set the new MeterProvider as the global MeterProvider.
        OpenTelemetrySdk.builder().setMeterProvider(meterProvider).buildAndRegisterGlobal();
    }

}
