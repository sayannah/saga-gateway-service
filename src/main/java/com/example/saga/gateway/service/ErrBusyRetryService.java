package com.example.saga.gateway.service;

import com.example.saga.gateway.websocket.GatewayWsClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.Set;

@Service
public class ErrBusyRetryService {

    private static final String RETRY_ZSET_KEY = "tx:errbusy:zset";

    private final StringRedisTemplate redis;
    private final GatewayWsClient wsClient;
    private final CircuitBreakerService circuitBreaker;

    public ErrBusyRetryService(StringRedisTemplate redis,
                               GatewayWsClient wsClient,
                               CircuitBreakerService circuitBreaker) {
        this.redis = redis;
        this.wsClient = wsClient;
        this.circuitBreaker = circuitBreaker;
    }

    public void scheduleRetry(String txId, String payload) {
        // Simple backoff: nextRetry = now + 5s
        long next = Instant.now().getEpochSecond() + 5;
        redis.opsForZSet().add(RETRY_ZSET_KEY, txId + "::" + payload, next);
        circuitBreaker.recordFailure();
    }

    @PostConstruct
    public void start() {
        Thread t = new Thread(this::loop, "errbusy-retry-loop");
        t.setDaemon(true);
        t.start();
    }

    private void loop() {
        while (true) {
            try {
                long now = Instant.now().getEpochSecond();
                Set<String> due = redis.opsForZSet()
                        .rangeByScore(RETRY_ZSET_KEY, 0, now);

                if (due != null) {
                    for (String entry : due) {
                        String[] parts = entry.split("::", 2);
                        String txId = parts[0];
                        String payload = parts[1];

                        if (circuitBreaker.allowRequest()) {
                            wsClient.sendToSystemB(txId, payload);
                            redis.opsForZSet().remove(RETRY_ZSET_KEY, entry);
                        }
                    }
                }

                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
