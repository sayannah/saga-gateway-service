package com.example.saga.gateway.service;

import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;

@Service
public class TransactionStateService {

    private final StringRedisTemplate redis;
    private final ObjectMapper mapper = new ObjectMapper();

    public TransactionStateService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void markReceived(String tx) {
        redis.opsForValue().set("tx:" + tx + ":state", "RECEIVED");
    }

    public void markProcessing(String tx) {
        redis.opsForValue().set("tx:" + tx + ":state", "PROCESSING");
    }

    public void applyBackendResult(String json) {
        try {
            Map map = mapper.readValue(json, Map.class);
            String tx = (String) map.get("transactionId");
            String result = (String) map.get("result");

            redis.opsForValue().set("tx:" + tx + ":state", result);
            System.out.println("State updated → " + tx + " = " + result);

        } catch (Exception ignored) {}
    }
}


