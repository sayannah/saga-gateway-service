package com.example.saga.gateway.websocket;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import jakarta.annotation.PostConstruct;
import java.net.URI;

@Lazy
@Component
public class GatewayWsClient {

    private final String wsUrl;
    private final GatewayWsHandler handler;

    private volatile WebSocketSession session;

    public GatewayWsClient(@Value("${SYSTEM_B_WS_URL}") String wsUrl,
                           @Lazy GatewayWsHandler handler) {
        this.wsUrl = wsUrl;
        this.handler = handler;
    }

    @PostConstruct
    public void start() {
        Thread t = new Thread(this::loop, "ws-client-loop");
        t.setDaemon(true);
        t.start();
    }

    private void loop() {
        while (true) {
            try {
                if (session == null || !session.isOpen()) {
                    connect();
                }
                Thread.sleep(1000);
            } catch (Exception ignored) {}
        }
    }

    private void connect() throws Exception {
        StandardWebSocketClient client = new StandardWebSocketClient();
        this.session = client.execute(handler, String.valueOf(URI.create(wsUrl))).get();
    }

    public WebSocketSession getSession() {
        return session;
    }
}
