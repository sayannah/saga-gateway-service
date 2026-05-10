package com.example.saga.gateway.service;

import com.example.saga.gateway.websocket.GatewayWsClient;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Service
public class PaymentForwarder {

    private final GatewayWsClient wsClient;

    public PaymentForwarder(GatewayWsClient wsClient) {
        this.wsClient = wsClient;
    }

    public void forwardToLedger(String txId, String payload) {
        try {
            WebSocketSession session = wsClient.getSession();
            if (session != null && session.isOpen()) {
                session.sendMessage(new TextMessage(payload));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
