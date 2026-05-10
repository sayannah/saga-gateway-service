package com.example.saga.gateway.websocket;

public interface WsMessageCallback {
        void onMessage(String txId, String status);
    }
