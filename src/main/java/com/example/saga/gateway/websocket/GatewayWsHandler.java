package com.example.saga.gateway.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class GatewayWsHandler extends TextWebSocketHandler {

    private final WsMessageCallback callback;
    private final ObjectMapper mapper = new ObjectMapper();

    public GatewayWsHandler(WsMessageCallback callback) {
        this.callback = callback;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            String payload = message.getPayload();

            JsonNode node = mapper.readTree(payload);

            String txId = node.get("transactionId").asText();
            String status = node.get("status").asText();

            callback.onMessage(txId, status);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
