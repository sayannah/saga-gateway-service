package com.example.saga.gateway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class ReverseWebhookService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String systemAUrl;

    public ReverseWebhookService(@Value("${systema.webhook.url}") String systemAUrl) {
        this.systemAUrl = systemAUrl;
    }

    public void sendAck(String txId, String finalStatus) {
        try {
            Map<String, Object> body = Map.of(
                    "transactionId", txId,
                    "status", finalStatus
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> resp =
                    restTemplate.postForEntity(systemAUrl, entity, String.class);

            System.out.println("Ack sent to System A → " + txId + " = " + finalStatus
                    + " (HTTP " + resp.getStatusCode() + ")");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

