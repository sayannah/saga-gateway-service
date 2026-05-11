package com.example.saga.gateway.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class TransactionStateService {

    private static final String STATE_KEY_PREFIX = "tx:state:";
    private static final String PAYLOAD_KEY_PREFIX = "tx:payload:";
    private static final String PENDING_COMP_KEY_PREFIX = "tx:pending-comp:";

    private final StringRedisTemplate redis;
    private final RedisLockService lockService;

    public TransactionStateService(StringRedisTemplate redis,
                                   RedisLockService lockService) {
        this.redis = redis;
        this.lockService = lockService;
    }

    public void handleEvent(String txId, String eventType, String payload) {
        if (!lockService.tryLock(txId)) {
            // Another instance is processing this tx; skip
            return;
        }
        try {
            switch (eventType) {
                case "RESERVE" -> handleReserve(txId, payload);
                case "COMMIT" -> handleCommit(txId);
                case "COMPENSATE" -> handleCompensate(txId);
                default -> System.out.println("Unknown eventType: " + eventType);
            }
        } finally {
            lockService.unlock(txId);
        }
    }

    private void handleReserve(String txId, String payload) {
        String stateKey = STATE_KEY_PREFIX + txId;
        String payloadKey = PAYLOAD_KEY_PREFIX + txId;
        String pendingCompKey = PENDING_COMP_KEY_PREFIX + txId;

        String current = redis.opsForValue().get(stateKey);
        if (current == null) {
            // First time: set RESERVED
            redis.opsForValue().set(stateKey, "RESERVED");
            redis.opsForValue().set(payloadKey, payload);
        }

        // If COMPENSATE arrived earlier, apply it now
        String pendingComp = redis.opsForValue().get(pendingCompKey);
        if (pendingComp != null) {
            // Apply rollback immediately
            redis.opsForValue().set(stateKey, "ROLLED_BACK");
            redis.delete(pendingCompKey);
        }
    }

    private void handleCommit(String txId) {
        String stateKey = STATE_KEY_PREFIX + txId;
        String current = redis.opsForValue().get(stateKey);
        if ("RESERVED".equals(current)) {
            redis.opsForValue().set(stateKey, "COMMITTED");
        } else {
            // Invalid transition; log and ignore
            System.out.println("COMMIT ignored for " + txId + " in state " + current);
        }
    }

    private void handleCompensate(String txId) {
        String stateKey = STATE_KEY_PREFIX + txId;
        String pendingCompKey = PENDING_COMP_KEY_PREFIX + txId;

        String current = redis.opsForValue().get(stateKey);
        if (current == null) {
            // RESERVE not yet seen; mark pending
            redis.opsForValue().set(pendingCompKey, "1");
        } else if ("RESERVED".equals(current)) {
            redis.opsForValue().set(stateKey, "ROLLED_BACK");
        } else if ("COMMITTED".equals(current)) {
            // Business decision: maybe log, maybe ignore
            System.out.println("COMPENSATE after COMMIT for " + txId);
        }
    }

}
