package com.example.saga.gateway.service;

import com.example.saga.gateway.processor.AsyncProcessor;
import com.example.saga.gateway.websocket.WsMessageCallback;
import org.springframework.stereotype.Service;

@Service
public class ErrBusyRetryService implements WsMessageCallback {

    private final AsyncProcessor asyncProcessor;

    public ErrBusyRetryService(AsyncProcessor asyncProcessor) {
        this.asyncProcessor = asyncProcessor;
    }

    @Override
    public void onMessage(String txId, String status) {
        asyncProcessor.handleWsResponse(txId, status);
    }
}
