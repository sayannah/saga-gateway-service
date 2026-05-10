package com.example.saga.gateway.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class TransactionStateService {

    private final StringRedisTemplate redis;
    private final ReverseWebhookService reverseWebhookService;

    public TransactionStateService(StringRedisTemplate redis,
                                   ReverseWebhookService reverseWebhookService) {
        this.redis = redis;
        this.reverseWebhookService = reverseWebhookService;
    }

    public void updateState(String txId, String status) {
        // 1. Update Redis
        redis.opsForValue().set("tx:" + txId + ":status", status);

        // 2. Trigger reverse webhook ONLY for terminal states
        if ("SUCCESS".equals(status) || "FAILED".equals(status)) {
            reverseWebhookService.sendAck(txId, status);
        }
    }

    public String getState(String txId) {
        return redis.opsForValue().get("tx:" + txId + ":status");
    }
}
