package com.example.saga.gateway.websocket;

import com.example.saga.gateway.service.ErrBusyRetryService;
import com.example.saga.gateway.service.ReverseWebhookService;
import com.example.saga.gateway.service.TransactionStateService;
import com.example.saga.gateway.util.JsonUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.stereotype.Component;

@Component
public class GatewayWsHandler extends TextWebSocketHandler {

    private final TransactionStateService stateService;
    private final ReverseWebhookService reverseWebhookService;
    private final ErrBusyRetryService errBusyRetryService;

    public GatewayWsHandler(TransactionStateService stateService,
                            ReverseWebhookService reverseWebhookService,
                            ErrBusyRetryService errBusyRetryService) {
        this.stateService = stateService;
        this.reverseWebhookService = reverseWebhookService;
        this.errBusyRetryService = errBusyRetryService;
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String incoming = message.getPayload();
        System.out.println("Gateway received WS: " + incoming);

        String txId = JsonUtils.extractField(incoming, "transactionId");
        String status = JsonUtils.extractField(incoming, "status");

        if ("ERR_BUSY".equals(status)) {
            errBusyRetryService.scheduleRetry(txId, incoming);
            return;
        }

        reverseWebhookService.sendAck(txId, status);
    }
}
