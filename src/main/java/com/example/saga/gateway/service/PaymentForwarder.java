package com.example.saga.gateway.service;

import com.example.saga.gateway.model.PaymentWebhookRequest;
import com.example.saga.gateway.websocket.GatewayWsClient;
import org.springframework.stereotype.Component;

@Component
public class PaymentForwarder {

    private final GatewayWsClient wsClient;
    private final TransactionStateService stateService;

    public PaymentForwarder(GatewayWsClient wsClient,
                            TransactionStateService stateService) {
        this.wsClient = wsClient;
        this.stateService = stateService;
    }

    public void forward(PaymentWebhookRequest req) {

        // 1. Mark state as PROCESSING
        stateService.markProcessing(req.getTransactionId());

        // 2. Build JSON payload for System B
        String json = """
                {
                  "transactionId": "%s",
                  "payload": "%s"
                }
                """.formatted(req.getTransactionId(), req.getPayload());

        // 3. Send to System B via WebSocket
        wsClient.send(json);

        System.out.println("Forwarded to System B → " + req.getTransactionId());
    }
}

