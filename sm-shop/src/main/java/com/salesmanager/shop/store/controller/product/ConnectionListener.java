package com.salesmanager.shop.store.controller.product;

import org.springframework.messaging.simp.stomp.StompSession;

/**
 * 
 * @author Reinhard.Pilz
 *
 */
public interface ConnectionListener {
    void connected(StompSession session);
}
