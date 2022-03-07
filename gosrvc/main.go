package main

import (
	"context"
	"fmt"
	"math/rand"
	"net/http"

	"go.opentelemetry.io/otel"
	"go.opentelemetry.io/otel/attribute"
	"go.opentelemetry.io/otel/codes"
	"go.opentelemetry.io/otel/exporters/otlp/otlptrace"
	"go.opentelemetry.io/otel/exporters/otlp/otlptrace/otlptracehttp"
	"go.opentelemetry.io/otel/propagation"
	"go.opentelemetry.io/otel/sdk/resource"
	sdktrace "go.opentelemetry.io/otel/sdk/trace"
	"go.opentelemetry.io/otel/trace"

	semconv "go.opentelemetry.io/otel/semconv/v1.7.0"
)

// newResource returns a resource describing this application.
func newResource() *resource.Resource {
	r, _ := resource.Merge(
		resource.Default(),
		resource.NewWithAttributes(
			semconv.SchemaURL,
			semconv.ServiceNameKey.String("gosrvc on port 8090"),
			semconv.ServiceVersionKey.String("v1.0.0"),
			attribute.String("environment", "hotday"),
		),
	)
	return r
}

func main() {

	client := otlptracehttp.NewClient(
		otlptracehttp.WithEndpoint("######.sprint.dynatracelabs.com"),
		otlptracehttp.WithURLPath("/api/v2/otlp/v1/traces"),
		otlptracehttp.WithHeaders(map[string]string{
			"Authorization": "Api-Token ############################################################################################",
		}),
	)

	exporter, _ := otlptrace.New(context.Background(), client)

	tp := sdktrace.NewTracerProvider(
		sdktrace.WithBatcher(exporter),
		sdktrace.WithResource(newResource()),
	)

	defer func() {
		tp.Shutdown(context.Background())
	}()
	otel.SetTracerProvider(tp)
	otel.SetTextMapPropagator(propagation.TraceContext{})

	http.HandleFunc("/quote", quote)
	http.HandleFunc("/calc", calc)
	http.ListenAndServe(":8090", nil)

}

func quote(w http.ResponseWriter, req *http.Request) {
	ctx := otel.GetTextMapPropagator().Extract(req.Context(), propagation.HeaderCarrier(req.Header))
	var span trace.Span
	ctx, span = otel.Tracer(name).Start(ctx, "quote", trace.WithSpanKind(trace.SpanKindServer))

	process(ctx, uint(rand.Intn(20)))
	span.End()
	fmt.Fprintf(w, "done\n")
}

func calc(w http.ResponseWriter, req *http.Request) {
	process(req.Context(), uint(rand.Intn(20)))
	fmt.Fprintf(w, "done\n")
}

func process(ctx context.Context, n uint) uint64 {
	f, _ := func(ctx context.Context) (uint64, error) {
		_, span := otel.Tracer(name).Start(ctx, "process", trace.WithAttributes(attribute.Int("n", int(n))))
		defer span.End()
		f, err := Fibonacci(n)
		if err != nil {
			span.RecordError(err)
			span.SetStatus(codes.Error, err.Error())
		}
		return f, err
	}(ctx)
	return f
}
