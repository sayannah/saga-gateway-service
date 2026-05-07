package com.example.saga_gateway_service.service;

import com.example.saga_gateway_service.model.PaymentWebhookRequest;

public interface PaymentService {
    public void saveState(PaymentWebhookRequest request);
    public void processAsync(PaymentWebhookRequest request);
}
