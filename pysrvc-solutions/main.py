import random
import requests
from flask import Flask, request, make_response
from opentelemetry import trace, propagate, context
from opentelemetry.instrumentation.flask import FlaskInstrumentor

from utils import process
from otel import ot


app = Flask("Py-Flask-App")
FlaskInstrumentor.instrument_app(app)


@app.route("/newroute", methods=["GET"])
def newroute():
    with ot.tracer.start_as_current_span("local route"):
        ctx = context.get_current()
        headers = {}
        propagate.inject(headers, ctx)
        requests.get("http://localhost:8080/shop", headers=headers)
        return make_response({}, 200)


@app.route("/quote", methods=["GET"])
def quote():
    with ot.tracer.start_as_current_span("quote") as span:
        trace.set_span_in_context(span, propagate.extract(request.headers))
        ot.metrics["requests_count"].add(1, {"request": "/quote"})
        process(random.randint(0, 25))
        return make_response({}, 200)


@app.route("/calc", methods=["GET"])
def calc():
    with ot.tracer.start_as_current_span("calc") as span:
        trace.set_span_in_context(span, propagate.extract(request.headers))
        ot.metrics["requests_count"].add(1, {"request": "/calc"})
        process(random.randint(0, 25))
        return make_response({}, 200)


def main():
    app.run(host='0.0.0.0', port=8090, debug=False)

    
if __name__ == "__main__":
    main()