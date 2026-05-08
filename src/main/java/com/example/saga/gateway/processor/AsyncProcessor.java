package com.example.saga.gateway.processor;

import com.example.saga.gateway.model.PaymentWebhookRequest;
import com.example.saga.gateway.service.PaymentForwarder;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class AsyncProcessor {

    private final BlockingQueue<PaymentWebhookRequest> queue = new LinkedBlockingQueue<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final PaymentForwarder forwarder;

    public AsyncProcessor(PaymentForwarder forwarder) {
        this.forwarder = forwarder;

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
        forwarder.forward(job);
    }
}


