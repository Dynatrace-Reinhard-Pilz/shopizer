import json
import random
from datetime import datetime
from flask import Flask, request, make_response
from opentelemetry.instrumentation.flask import FlaskInstrumentor
from opentelemetry import trace, propagate
from opentelemetry.trace.status import Status, StatusCode

from fib import fibonacci
from otel import otel_setup, requests_metric, proc_duration_metric


app = Flask("Py-Flask-App")
FlaskInstrumentor.instrument_app(app)


@app.route("/quote", methods=["GET"])
def quote():
    requests_metric().add(1, {"request": "/quote"})
    trace.set_span_in_context(trace.get_current_span(), propagate.extract(request.headers))
    process(random.randint(0, 22))
    print("done")
    return make_response({}, 200)


@app.route("/calc", methods=["GET"])
def calc():
    requests_metric().add(1, {"request": "/calc"})
    trace.set_span_in_context(trace.get_current_span(), propagate.extract(request.headers))
    process(random.randint(0, 22))
    print("done")
    return make_response({}, 200)


def process(n: int) -> int:
    with trace.get_tracer("custom").start_as_current_span("process") as span:
        span.set_attribute("n", n)
        try:
            span.add_event("Calculating Fibonnaci", {"n": n})
            start = datetime.now()
            f = fibonacci(n)
            duration = (datetime.now().timestamp() - start.timestamp())*1000
            proc_duration_metric().record(duration, {"number": n})
            return f
        except Exception as e:
            span.record_exception(e)
            span.set_status(Status(StatusCode.ERROR, e))


def main():
    otel_setup()
    app.run(host='0.0.0.0', port=8090, debug=False)

    
if __name__ == "__main__":
    main()