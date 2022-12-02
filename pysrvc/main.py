import random
from flask import Flask, request, make_response
from opentelemetry import trace, propagate
# Example exercise - add Flask auto-instrumentation
from opentelemetry.instrumentation.flask import FlaskInstrumentor

from utils import process
from otel import ot


app = Flask("Py-Flask-App")
# Example exercise - add Flask auto-instrumentation
FlaskInstrumentor.instrument_app(app)


@app.route("/quote", methods=["GET"])
def quote():
    ot.metrics["requests_count"].add(1, {"request": "/quote"})
    trace.set_span_in_context(trace.get_current_span(), propagate.extract(request.headers))
    process(random.randint(0, 25))
    print("done")
    return make_response({}, 200)


@app.route("/calc", methods=["GET"])
def calc():
    ot.metrics["requests_count"].add(1, {"request": "/calc"})
    # Example exercise - create a Span, set it in Context
    trace.set_span_in_context(trace.get_current_span(), propagate.extract(request.headers))
    process(random.randint(0, 25))
    print("done")
    return make_response({}, 200)


def main():
    app.run(host='0.0.0.0', port=8090, debug=False)

    
if __name__ == "__main__":
    main()