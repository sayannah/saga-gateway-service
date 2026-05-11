package com.example.saga.gateway.processor;

import com.example.saga.gateway.service.ErrBusyRetryService;
import com.example.saga.gateway.service.TransactionStateService;
import com.example.saga.gateway.websocket.GatewayWsClient;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;

@Component
public class AsyncProcessor {

    private static final String STREAM_KEY = "tx-events-stream";

    private final StringRedisTemplate redis;
    private final TransactionStateService stateService;
    private final GatewayWsClient wsClient;
    private final ErrBusyRetryService errBusyRetryService;

    public AsyncProcessor(StringRedisTemplate redis,
                          TransactionStateService stateService,
                          GatewayWsClient wsClient,
                          ErrBusyRetryService errBusyRetryService) {
        this.redis = redis;
        this.stateService = stateService;
        this.wsClient = wsClient;
        this.errBusyRetryService = errBusyRetryService;
    }

    @PostConstruct
    public void start() {
        Thread t = new Thread(this::loop, "async-processor-loop");
        t.setDaemon(true);
        t.start();
    }

    private void loop() {
        while (true) {
            try {
                List<MapRecord<String, Object, Object>> records =
                        redis.opsForStream().read(StreamOffset.fromStart(STREAM_KEY));

                if (records == null || records.isEmpty()) {
                    Thread.sleep(50);
                    continue;
                }

                for (MapRecord<String, Object, Object> rec : records) {
                    String txId = (String) rec.getValue().get("transactionId");
                    String payload = (String) rec.getValue().get("payload");
                    String eventType = (String) rec.getValue().get("eventType");

                    // Update saga state
                    stateService.handleEvent(txId, eventType, payload);

                    // For RESERVE/COMMIT, forward to System B
                    if ("RESERVE".equals(eventType) || "COMMIT".equals(eventType)) {
                        wsClient.sendToSystemB(txId, payload);
                    }

                    // Acknowledge record (simple: delete from stream)
                    redis.opsForStream().delete(STREAM_KEY, rec.getId());
                }

            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ignored) {}
            }
        }
    }
}
