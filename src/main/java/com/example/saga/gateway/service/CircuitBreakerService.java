package com.example.saga.gateway.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class CircuitBreakerService {

    private enum State { CLOSED, OPEN, HALF_OPEN }

    private volatile State state = State.CLOSED;
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicLong openedAt = new AtomicLong(0);

    private static final int FAILURE_THRESHOLD = 5;
    private static final long OPEN_DURATION_SEC = 10;

    public void recordFailure() {
        int failures = failureCount.incrementAndGet();
        if (state == State.CLOSED && failures >= FAILURE_THRESHOLD) {
            state = State.OPEN;
            openedAt.set(Instant.now().getEpochSecond());
            System.out.println("Circuit OPEN");
        }
    }

    public boolean allowRequest() {
        long now = Instant.now().getEpochSecond();
        if (state == State.OPEN) {
            if (now - openedAt.get() > OPEN_DURATION_SEC) {
                state = State.HALF_OPEN;
                failureCount.set(0);
                return true;
            }
            return false;
        } else if (state == State.HALF_OPEN) {
            // Allow a few test requests
            return true;
        }
        return true; // CLOSED
    }

}
