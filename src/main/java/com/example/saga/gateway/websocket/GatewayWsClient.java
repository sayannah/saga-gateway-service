package com.example.saga.gateway.websocket;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class GatewayWsClient {

    private final String wsUrl;
    private final GatewayWsHandler handler;

    private volatile WebSocketSession session;
    private final BlockingQueue<String> outboundQueue = new LinkedBlockingQueue<>();

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

        Thread sender = new Thread(this::sendLoop, "ws-sender-loop");
        sender.setDaemon(true);
        sender.start();
    }

    private void loop() {
        while (true) {
            try {
                if (session == null || !session.isOpen()) {
                    connect();
                }
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {}
            }
        }
    }

    private void connect() throws Exception {
        StandardWebSocketClient client = new StandardWebSocketClient();
        this.session = client.execute(handler, wsUrl).get();
        System.out.println("Gateway WS connected to System B");
    }

    private void sendLoop() {
        while (true) {
            try {
                String msg = outboundQueue.take();
                if (session != null && session.isOpen()) {
                    session.sendMessage(new TextMessage(msg));
                } else {
                    // Requeue if not connected
                    outboundQueue.offer(msg);
                    Thread.sleep(500);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sendToSystemB(String txId, String payload) {
        // You can send raw payload or a wrapped message
        outboundQueue.offer(payload);
    }

    public WebSocketSession getSession() {
        return session;
    }
}
