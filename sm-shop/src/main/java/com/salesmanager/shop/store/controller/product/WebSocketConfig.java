package com.salesmanager.shop.store.controller.product;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ExecutorChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.messaging.support.NativeMessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;
import org.springframework.web.socket.messaging.WebSocketAnnotationMethodMessageHandler;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;

/**
 * 
 * @author Reinhard.Pilz
 *
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer implements ExecutorChannelInterceptor, TextMapGetter<Message<?>> {
	
	private static Logger LOG = LoggerFactory.getLogger(WebSocketConfig.class);
	
	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		config.enableSimpleBroker("/topic");
		config.setApplicationDestinationPrefixes("/app");
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/chat").setAllowedOrigins("*").withSockJS();
	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.setInterceptors(this);
	}
	
	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		return message;		
	}

	@Override
	public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
	}

	@Override
	public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
	}

	@Override
	public boolean preReceive(MessageChannel channel) {
		return true;
	}

	@Override
	public Message<?> postReceive(Message<?> message, MessageChannel channel) {
		return message;
	}

	@Override
	public void afterReceiveCompletion(Message<?> message, MessageChannel channel, Exception ex) {
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, List<String>> getNativeHeaders(MessageHeaderAccessor accessor) {
		return (Map<String, List<String>>)accessor.getHeader(NativeMessageHeaderAccessor.NATIVE_HEADERS);
	}

	@Override
	public synchronized Message<?> beforeHandle(Message<?> message, MessageChannel channel, MessageHandler handler) {
		if (!(handler instanceof WebSocketAnnotationMethodMessageHandler)) {
			return message;
		}
		LOG.info("beforeHandle");
		Context context = GlobalOpenTelemetry.getPropagators().getTextMapPropagator().extract(Context.current(), message, this);
		try (Scope scope = context.makeCurrent()) {
			String destination = this.get(message, "destination");
			Tracer tracer = GlobalOpenTelemetry.getTracer("shop-product", "1.0.0");
			Span span = tracer
					.spanBuilder("RECEIVE " + destination)
					.setSpanKind(SpanKind.CONSUMER)					
					.setAttribute("messaging.destination", destination).startSpan();
			span.end();
		}		
		return message;		
	}


	@Override
	public void afterMessageHandled(Message<?> message, MessageChannel channel, MessageHandler handler, Exception ex) {
	}

	@Override
	public Iterable<String> keys(Message<?> message) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
		if (accessor != null) {
			Map<String, List<String>> nativeHeaders = getNativeHeaders(accessor);
			if (nativeHeaders != null) {
				return nativeHeaders.keySet();
			}
		}		
		return Collections.emptyList();
	}

	@Override
	public String get(Message<?> message, String key) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
		if (accessor != null) {
			Map<String, List<String>> nativeHeaders = getNativeHeaders(accessor);
			if (nativeHeaders != null) {
				List<String> values = nativeHeaders.get(key);
				if ((values != null) && !values.isEmpty()) {
					return values.get(0);
				}
			}
		}
		return null;
	}

}
