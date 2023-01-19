package com.salesmanager.shop.store.controller.product;

import java.util.Collections;
import java.util.Objects;

import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapSetter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Reinhard.Pilz
 *
 */
public final class TaggingStompSession implements StompSession, TextMapSetter<StompHeaders> {

	private static final Logger LOGGER = LoggerFactory.getLogger(TaggingStompSession.class);
	
	private final StompSession session;
	
	public TaggingStompSession(StompSession session) {
		Objects.requireNonNull(session);
		this.session = session;
	}
	
    @Override
    public void set(StompHeaders headers, String key, String value) {
    	headers.put(key, Collections.singletonList(value));        
    }

	@Override
	public Receiptable send(String destination, Object payload) {
		StompHeaders stompHeaders = new StompHeaders();
		stompHeaders.setDestination(destination);
		return send(stompHeaders, payload);
	}    
    
	@Override
	public Receiptable send(StompHeaders headers, Object payload) {
		Tracer tracer = GlobalOpenTelemetry.getTracer("java-websocket", "1.0.0");
		Span span = tracer.spanBuilder("SEND " + headers.getDestination()).setSpanKind(SpanKind.PRODUCER).setAttribute("messaging.destination", headers.getDestination()).startSpan();
		try (Scope scope = span.makeCurrent()) {
			SpanContext spanContext = Span.current().getSpanContext();
			GlobalOpenTelemetry.getPropagators().getTextMapPropagator().inject(Context.current(), headers, this);		
			LOGGER.info("[!dt dt.trace_id=" + spanContext.getTraceId() + ",dt.span_id=" + spanContext.getSpanId() + "] sending message to " + headers.getDestination());
			return session.send(headers, payload);
		} finally {
			span.end();
		}		
	}

	@Override
	public String getSessionId() {
		return session.getSessionId();
	}

	@Override
	public boolean isConnected() {
		return session.isConnected();
	}

	@Override
	public void setAutoReceipt(boolean enabled) {
		session.setAutoReceipt(enabled);
	}

	@Override
	public Subscription subscribe(String destination, StompFrameHandler handler) {
		return session.subscribe(destination, handler);
	}

	@Override
	public Subscription subscribe(StompHeaders headers, StompFrameHandler handler) {
		return session.subscribe(headers, handler);
	}

	@Override
	public Receiptable acknowledge(String messageId, boolean consumed) {
		return session.acknowledge(messageId, consumed);
	}

	@Override
	public void disconnect() {
		session.disconnect();
	}

	@Override
	public Receiptable acknowledge(StompHeaders headers, boolean consumed) {
		return session.acknowledge(headers, consumed);
	}

	@Override
	public void disconnect(StompHeaders headers) {
		session.disconnect(headers);
	}

}
