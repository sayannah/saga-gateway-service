package com.example.saga.gateway.processor;

import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class QueuePublisher {

    private static final String STREAM_KEY = "tx-events-stream";

    private final StringRedisTemplate redis;

    public QueuePublisher(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void publish(String txId, String rawJson, String eventType) {

        if (txId == null || eventType == null || rawJson == null) {
            throw new IllegalArgumentException("Null values passed to QueuePublisher");
        }

        var record = StreamRecords.newRecord()
                .in(STREAM_KEY)
                .ofMap(Map.of(
                        "transactionId", txId,
                        "payload", rawJson,
                        "eventType", eventType
                ));

        redis.opsForStream().add(record);
    }

}
