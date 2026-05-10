package com.example.saga.gateway.processor;


import org.springframework.data.redis.connection.stream.RecordId;
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

    public void publish(String txId, String rawJson) {
        try {
            var record = StreamRecords.newRecord()
                    .in(STREAM_KEY)
                    .ofMap(Map.of(
                            "transactionId", txId,
                            "payload", rawJson
                    ));

            RecordId id = redis.opsForStream().add(record);

            System.out.println("Enqueued → " + txId + " @ " + id);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
