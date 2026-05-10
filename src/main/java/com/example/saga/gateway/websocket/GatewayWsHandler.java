package com.example.saga.gateway.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class GatewayWsHandler extends TextWebSocketHandler {

    private final WsMessageCallback callback;

    public GatewayWsHandler(WsMessageCallback callback) {
        this.callback = callback;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();

        // parse txId + status from JSON
        String txId = extractTxId(payload);
        String status = extractStatus(payload);

        callback.onMessage(txId, status);
    }

    private String extractTxId(String json) {
        return json.split("\"transactionId\":\"")[1].split("\"")[0];
    }

    private String extractStatus(String json) {
        return json.split("\"status\":\"")[1].split("\"")[0];
    }
}