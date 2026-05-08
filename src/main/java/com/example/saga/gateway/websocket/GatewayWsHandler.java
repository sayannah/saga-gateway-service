package com.example.saga.gateway.websocket;

import com.example.saga.gateway.service.TransactionStateService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class GatewayWsHandler extends TextWebSocketHandler {

    private WebSocketSession session;
    private final TransactionStateService stateService;

    public GatewayWsHandler(TransactionStateService stateService) {
        this.stateService = stateService;
    }

    @Override
    public void afterConnectionEstablished(@NotNull WebSocketSession session) {
        this.session = session;
        System.out.println("Gateway WS: handler session established");
    }

    @Override
    public void handleTextMessage(@NotNull WebSocketSession session, TextMessage message) {
        stateService.applyBackendResult(message.getPayload());
    }

    public WebSocketSession getSession() {
        return session;
    }
}
