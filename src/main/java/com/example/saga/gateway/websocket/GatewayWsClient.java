package com.example.saga.gateway.websocket;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.net.URI;
import java.util.concurrent.ExecutionException;

@Component
public class GatewayWsClient {

    private final GatewayWsHandler handler;
    private WebSocketSession session;

    public GatewayWsClient(GatewayWsHandler handler) {
        this.handler = handler;
    }

    @PostConstruct
    public void connect() {
        try {
            StandardWebSocketClient client = new StandardWebSocketClient();

            this.session = client.execute(
                    handler,
                    String.valueOf(URI.create("ws://external-ledger:6002/mock-ws"))
            ).get();

            System.out.println("Gateway WS: connected to System B");

        } catch (InterruptedException | ExecutionException e) {
            System.out.println("Gateway WS failed: " + e.getMessage());
        }
    }

    public void send(String json) {
        try {
            if (session != null && session.isOpen()) {
                session.sendMessage(new TextMessage(json));
            }
        } catch (Exception ignored) {}
    }
}
