package com.salesmanager.shop.store.controller.product;

import org.springframework.boot.CommandLineRunner;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.stereotype.Component;

/**
 * 
 * @author Reinhard.Pilz
 *
 */
@Component
public class MessageClientRunner implements CommandLineRunner, ConnectionListener {
	
	private static final Object LOCK = new Object();
	private static StompSession session;

	@Override
	public void run(String... args) throws Exception {
		MessageClient.init(this);
	}
	
	public static StompSession getSession() {
		synchronized (LOCK) {
			return session;
		}
	}

	@Override
	public final void connected(final StompSession session) {
		synchronized (LOCK) {
			MessageClientRunner.session = session;
		}		
	}	

}
