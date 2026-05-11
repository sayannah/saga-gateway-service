package com.example.saga.gateway.controller;

import com.example.saga.gateway.config.SignatureVerifier;
import com.example.saga.gateway.processor.QueuePublisher;
import com.example.saga.gateway.util.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1")
public class WebhookController {

    private final SignatureVerifier signatureVerifier;
    private final QueuePublisher queuePublisher;

    public WebhookController(SignatureVerifier signatureVerifier,
                             QueuePublisher queuePublisher) {
        this.signatureVerifier = signatureVerifier;
        this.queuePublisher = queuePublisher;
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestHeader("X-Timestamp") String timestamp,
            @RequestHeader("X-Signature") String signature,
            @RequestBody String rawJson) {

        long start = System.nanoTime();

        // 1. Signature verification (can be disabled via env)
        if (!signatureVerifier.verifyRequest(rawJson, signature, timestamp)) {
            log.warn("Signature verification failed");
            return ResponseEntity.status(403).build();
        }

        // 2. Parse JSON safely
        JsonNode node = JsonUtils.parse(rawJson);
        if (node == null) {
            log.error("Invalid JSON payload");
            return ResponseEntity.badRequest().build();
        }

        // 3. Extract required fields
        String txId = node.path("transactionId").asText(null);
        String eventType = node.path("eventType").asText(null);

        if (txId == null || eventType == null) {
            log.error("Missing required fields: txId={}, eventType={}", txId, eventType);
            return ResponseEntity.badRequest().build();
        }

        // 4. Publish to Redis Stream
        queuePublisher.publish(txId, rawJson, eventType);

        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        log.info("Webhook accepted in {} ms", elapsedMs);

        return ResponseEntity.accepted().build();
    }
}
