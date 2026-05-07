package com.example.saga_gateway_service.controller;

import com.example.saga_gateway_service.config.SignatureVerifier;
import com.example.saga_gateway_service.model.PaymentWebhookRequest;
import com.example.saga_gateway_service.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {

    @Autowired
    private PaymentService paymentService;

    private final RedisTemplate<String, Object> redisTemplate;

    public WebhookController(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostMapping("/test")
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
    @PostMapping("/payment")
    public ResponseEntity<String> verifyRequest(
            @RequestBody PaymentWebhookRequest request,
            @RequestHeader("X-Signature") String signature,
            @RequestHeader("X-Public-Key") String publicKey) {

        byte[] rawBody = request.getPayload().getBytes(StandardCharsets.UTF_8);

        // 1. Verify signature
        if (!SignatureVerifier.verify(rawBody, signature, publicKey)) {
            return ResponseEntity.badRequest().body("invalid signature");
        }

        // 2. Save state (DB, Redis, etc.)
        paymentService.saveState(request);

        // 3. Push async job
        paymentService.processAsync(request);

        // 4. Return immediately
        return ResponseEntity.accepted().body("accepted");
    }

}
