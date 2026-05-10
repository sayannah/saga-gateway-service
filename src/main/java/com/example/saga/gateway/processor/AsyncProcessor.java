package com.example.saga.gateway.processor;

import com.example.saga.gateway.service.PaymentForwarder;
import com.example.saga.gateway.service.TransactionStateService;
import org.springframework.stereotype.Component;

@Component
public class AsyncProcessor {

    private final PaymentForwarder forwarder;
    private final TransactionStateService stateService;

    public AsyncProcessor(PaymentForwarder forwarder,
                          TransactionStateService stateService) {
        this.forwarder = forwarder;
        this.stateService = stateService;
    }

    public void handleWsResponse(String txId, String status) {
        stateService.updateState(txId, status);
    }

    public void process(String txId, String payload) {
        forwarder.forwardToLedger(txId, payload);
    }
}
