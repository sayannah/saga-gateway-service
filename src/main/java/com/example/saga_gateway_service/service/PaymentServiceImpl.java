package com.example.saga_gateway_service.service;

import com.example.saga_gateway_service.model.PaymentWebhookRequest;
import com.example.saga_gateway_service.processor.AsyncProcessor;
import org.springframework.stereotype.Service;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final AsyncProcessor processor;

    public PaymentServiceImpl(AsyncProcessor processor) {
        this.processor = processor;
    }

    public void saveState(PaymentWebhookRequest request) {
        // persist request metadata
    }

    public void processAsync(PaymentWebhookRequest request) {
        processor.submit(request);
    }
}

