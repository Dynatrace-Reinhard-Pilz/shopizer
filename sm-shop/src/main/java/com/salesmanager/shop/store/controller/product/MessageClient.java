package com.salesmanager.shop.store.controller.product;

import java.util.ArrayList;
import java.util.List;

import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

/**
 * 
 * @author Reinhard.Pilz
 *
 */
public class MessageClient extends StompSessionHandlerAdapter {
	
//	private static Logger LOG = LoggerFactory.getLogger(MessageClient.class);
	
	private final ConnectionListener listener;
	
	public static void init(ConnectionListener listener) {
		new MessageClient(listener);
	}
	
	private MessageClient(ConnectionListener listener) {
		this.listener = listener;
		WebSocketClient simpleWebSocketClient = new StandardWebSocketClient();
		List<Transport> transports = new ArrayList<>(1);
		transports.add(new WebSocketTransport(simpleWebSocketClient));
		SockJsClient sockJsClient = new SockJsClient(transports);
		WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
		stompClient.setMessageConverter(new MappingJackson2MessageConverter());
		String url = "ws://127.0.0.1:8080/chat";
		synchronized (this) {
			try {
				stompClient.connect(url, this).get();
			} catch (Throwable t) {
				throw new InternalError(t);
			}		
		}	
	}
	
	@Override
	public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
		listener.connected(new TaggingStompSession(session));
	}

}
