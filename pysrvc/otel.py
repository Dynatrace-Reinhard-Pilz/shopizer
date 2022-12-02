import json
import psutil
from opentelemetry import trace, metrics
from opentelemetry.sdk.resources import Resource
from opentelemetry.semconv.resource import ResourceAttributes
from opentelemetry.sdk.trace import TracerProvider, sampling
from opentelemetry.sdk.trace.export import BatchSpanProcessor
from opentelemetry.exporter.otlp.proto.http.trace_exporter import OTLPSpanExporter
from opentelemetry.sdk.metrics import MeterProvider
from opentelemetry.metrics import CallbackOptions, Observation
from dynatrace.opentelemetry.metrics.export import configure_dynatrace_metrics_export


"""Gathers CPU usage, reported as an OpenTelemtry observation"""
def get_cpu_usage(_: CallbackOptions):
    for (number, percent) in enumerate(psutil.cpu_percent(percpu=True)):
        attributes = {"cpu_number": str(number)}
        yield Observation(percent, attributes)


"""A class for controlling custom OpenTelemetry behavior"""
class CustomOpenTelemetry():
    def __init__(self):
        self.resource_props = self.get_resource_props()
        self.setup_exporters()
        self.metrics = {}
        self.meter = metrics.get_meter("perform-hot")
        self.meter.create_observable_gauge(
            callbacks=[get_cpu_usage],
            name="cpu_usage",
            description="CPU Usage per processor, as percentage",
            unit="1"
        )
        self.create_counter_instrument(
            "requests_count",
            "Counts the number of requests to the service"
        )
        # Example exercise - create a histogram metric instrument
        self.create_histogram_instrument(
            "process_duration",
            "Duration of Fibonacci calculation, in milliseconds",
            "ms"
        )

    """Returns a resource properties describing this application."""
    def get_resource_props(self):
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
    def setup_exporters(self):
        # Set up trace export
        tracer_provider = TracerProvider(
            sampler=sampling.ALWAYS_ON,
            resource=Resource(self.resource_props)
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
                    prefix="perform.opentelemetry.hot",
                    default_dimensions=self.resource_props
                )
            ]
        ))

    """Creates a synchronous counter instrument with given details"""
    def create_counter_instrument(self, name: str, description: str):
        self.metrics[name] = self.meter.create_counter(
            name=name, 
            description=description, 
            unit="1"
        )

    # Example exercise - create a histogram metric instrument
    """Creates a synchronous histogram instrument with given details"""
    def create_histogram_instrument(self, name: str, description: str, unit: str):
        self.metrics[name] = self.meter.create_histogram(
            name=name,
            description=description,
            unit=unit
        )

# Sets everything up and can be reused anywhere in the code
ot = CustomOpenTelemetry()