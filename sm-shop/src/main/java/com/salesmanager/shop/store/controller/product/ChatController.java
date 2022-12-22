package com.salesmanager.shop.store.controller.product;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;

/**
 * 
 * @author Reinhard.Pilz
 *
 */
@Controller
public class ChatController {
	
	private static Logger LOG = LoggerFactory.getLogger(ChatController.class);
	
	@MessageMapping("/chat/{topic}")
	public void send(@DestinationVariable("topic") String topic, Message message) throws Exception {
		LOG.info("message received");
	}
	
}
