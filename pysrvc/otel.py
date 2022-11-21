from opentelemetry import trace, metrics
from opentelemetry.sdk.resources import Resource
from opentelemetry.semconv.resource import ResourceAttributes
from opentelemetry.sdk.trace import TracerProvider, sampling
from opentelemetry.sdk.trace.export import BatchSpanProcessor
from opentelemetry.exporter.otlp.proto.http.trace_exporter import OTLPSpanExporter
from opentelemetry.sdk.metrics import MeterProvider
from dynatrace.opentelemetry.metrics.export import configure_dynatrace_metrics_export


"""Returns a resource describing this application."""
def get_resource_props():
    # Basic resource details
    resource = {
        ResourceAttributes.SERVICE_NAME: "pysrvc svc on port 8090",
        ResourceAttributes.SERVICE_VERSION: "v1.0.0",
        "environment": "hotday"
    }
    # Get OneAgent's topology metadata and add to resource
    try:
        data = ''
        with open("dt_metadata_e617c525669e072eebe3d0f08212e8f2.json") as f:
            data = json.load(open(f.read()))
        resource.update(data)
    except:
        pass    

    return resource


"""Sets up OpenTelemetry Trace & Metrics export"""
def otel_setup():
    resource_props = get_resource_props()
    # Set up trace export
    tracer_provider = TracerProvider(
        sampler=sampling.ALWAYS_ON,
        resource=Resource(resource_props)
    )
    tracer_provider.add_span_processor(
        BatchSpanProcessor(OTLPSpanExporter(
            endpoint="http://localhost:14499/otlp/v1/traces"
        ))
    )
    trace.set_tracer_provider(tracer_provider)

    # Set up metrics export
    metrics.set_meter_provider(MeterProvider(
        metric_readers=[
            configure_dynatrace_metrics_export(
                export_dynatrace_metadata=True,
                prefix="custom.opentelemetry",
                default_dimensions=resource_props
            )
        ]
    ))


def requests_metric():
    return metrics.get_meter("custom").create_counter(
        name="requests_count", 
        description="Number of custom requests", 
        unit="1"
    )

def proc_duration_metric():
    return metrics.get_meter("custom").create_histogram(
        name="process_duration",
        description="Duration of Fibonacci processing in Milliseconds",
        unit="ms"
    )
