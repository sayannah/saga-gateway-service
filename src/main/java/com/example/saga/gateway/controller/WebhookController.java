package com.example.saga.gateway.controller;

import com.example.saga.gateway.config.SignatureVerifier;
import com.example.saga.gateway.model.PaymentWebhookRequest;
import com.example.saga.gateway.processor.AsyncProcessor;
import com.example.saga.gateway.service.TransactionStateService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {

    private final SignatureVerifier signatureVerifier;
    private final TransactionStateService stateService;
    private final AsyncProcessor asyncProcessor;
    private final RedisTemplate<String, Object> redisTemplate;

    public WebhookController(SignatureVerifier signatureVerifier,
                             TransactionStateService stateService,
                             AsyncProcessor asyncProcessor, RedisTemplate<String, Object> redisTemplate) {
        this.signatureVerifier = signatureVerifier;
        this.stateService = stateService;
        this.asyncProcessor = asyncProcessor;
        this.redisTemplate = redisTemplate;
    }

    @PostMapping("/test")
    public void receiveWebhook(@RequestBody Map<String, Object> payload) {
        // 1. Print the incoming JSON payload
        System.out.println("Received Payload: " + payload);

        // 2. Extract transactionId and status
        String transactionId = (String) payload.get("transactionId");
        String status = (String) payload.get("status");

        if (transactionId != null && status != null) {
            // 3. Store in Redis: transactionId -> status
            redisTemplate.opsForValue().set(transactionId, status);
            System.out.println("Stored: " + transactionId + " -> " + status);
        }
    }
    @PostMapping("/payment")
    public ResponseEntity<String> receive(
            @RequestBody PaymentWebhookRequest req,
            @RequestHeader("X-Signature") String signature,
            @RequestHeader("X-Timestamp") String timestamp) {

        String rawJson = """
                {
                  "transactionId": "%s",
                  "payload": "%s",
                  "stamp": "%s"
                }
                """.formatted(req.getTransactionId(), req.getPayload(), req.getStamp());

        if (!signatureVerifier.verifyRequest(rawJson, signature, timestamp)) {
            return ResponseEntity.status(403).body("invalid signature");
        }

        stateService.markReceived(req.getTransactionId());
        asyncProcessor.submit(req);

        return ResponseEntity.accepted().body("accepted");
    }

}
