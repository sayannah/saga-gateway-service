package com.example.saga.gateway.service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class ReverseWebhookService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String ackUrl;

    public ReverseWebhookService() {
        this.ackUrl = System.getenv("systema.webhook.url");
    }

    public void sendAck(String txId, String status) {
        try {
            var body = new AckPayload(txId, status);
            restTemplate.postForEntity(ackUrl, body, String.class);
            log.info("ACK delivered to System A: {} {}", txId, status);
        } catch (Exception e) {
            log.warn("ACK delivery failed (System A offline): {}", e.getMessage());
        }
    }

    record AckPayload(String transactionId, String status) {}
}
