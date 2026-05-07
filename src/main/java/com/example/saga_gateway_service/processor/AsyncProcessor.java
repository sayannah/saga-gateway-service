package com.example.saga_gateway_service.processor;

import com.example.saga_gateway_service.model.PaymentWebhookRequest;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class AsyncProcessor {

    private final BlockingQueue<PaymentWebhookRequest> queue = new LinkedBlockingQueue<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public AsyncProcessor() {
        executor.submit(() -> {
            while (true) {
                PaymentWebhookRequest job = queue.take();
                handle(job);
            }
        });
    }

    public void submit(PaymentWebhookRequest job) {
        queue.offer(job);
    }

    private void handle(PaymentWebhookRequest job) {
        System.out.println("Processing async payment: " + job);
        // heavy work here
    }
}

