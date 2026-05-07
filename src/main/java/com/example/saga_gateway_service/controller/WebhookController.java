package com.example.saga_gateway_service.controller;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {
    private final RedisTemplate<String, Object> redisTemplate;

    public WebhookController(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostMapping("/payment")
    public void receiveWebhook(@RequestBody Map<String, Object> payload) {
        // 1. Print the incoming JSON payload
        System.out.println("Received Payload: " + payload);

        // 2. Extract transactionId and status
        // Cast to String assuming the JSON structure: {"transactionId": "123", "status": "SUCCESS"}
        String transactionId = (String) payload.get("transactionId");
        String status = (String) payload.get("status");

        if (transactionId != null && status != null) {
            // 3. Store in Redis: transactionId -> status
            redisTemplate.opsForValue().set(transactionId, status);
            System.out.println("Stored: " + transactionId + " -> " + status);
        }
    }
}
