package com.example.saga.gateway.controller;

import com.example.saga.gateway.config.SignatureVerifier;
import com.example.saga.gateway.processor.AsyncProcessor;
import com.example.saga.gateway.service.TransactionStateService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/webhook")
public class WebhookController {

    private final SignatureVerifier signatureVerifier;
    private final TransactionStateService stateService;
    private final AsyncProcessor asyncProcessor;

    public WebhookController(SignatureVerifier signatureVerifier,
                             TransactionStateService stateService,
                             AsyncProcessor asyncProcessor) {
        this.signatureVerifier = signatureVerifier;
        this.stateService = stateService;
        this.asyncProcessor = asyncProcessor;
    }

    @PostMapping
    public ResponseEntity<?> receiveWebhook(
            @RequestHeader("X-Signature") String signature,
            @RequestHeader("X-Timestamp") String timestamp,
            @RequestBody String payload) {

        if (!signatureVerifier.verifyRequest(payload, signature, timestamp)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid signature");
        }

        String txId = extractTxId(payload);

        stateService.updateState(txId, "RECEIVED");
        asyncProcessor.process(txId, payload);

        return ResponseEntity.accepted().body("accepted");
    }


    private String extractTxId(String json) {
        return json.split("\"transactionId\":\"")[1].split("\"")[0];
    }
}
