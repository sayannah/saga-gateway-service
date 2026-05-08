package com.example.saga.gateway.model;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class PaymentWebhookRequest {
    private String transactionId;
    private String payload;
    private String signature;
    private String stamp;
}