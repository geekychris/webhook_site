package com.example.webhooksite.service;

import com.example.webhooksite.model.Webhook;
import com.example.webhooksite.model.WebhookRequest;
import com.example.webhooksite.repository.WebhookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class WebhookService {
    private final WebhookRepository webhookRepository;

    public WebhookService(WebhookRepository webhookRepository) {
        this.webhookRepository = webhookRepository;
    }

    @Transactional
    public Webhook createWebhook(String customPath) {
        String path = customPath != null && !customPath.isEmpty() 
            ? customPath 
            : UUID.randomUUID().toString();
            
        if (webhookRepository.existsByPath(path)) {
            throw new IllegalArgumentException("Path already exists");
        }

        Webhook webhook = new Webhook();
        webhook.setPath(path);
        return webhookRepository.save(webhook);
    }

    @Transactional
    public WebhookRequest recordWebhookRequest(String path, String method, String body, Map<String, String> headers) {
        Webhook webhook = webhookRepository.findByPath(path)
            .orElseThrow(() -> new IllegalArgumentException("Webhook not found"));

        WebhookRequest request = new WebhookRequest();
        request.setWebhook(webhook);
        request.setMethod(method);
        request.setBody(body);
        request.setHeaders(headers);

        webhook.getRequests().add(request);
        webhookRepository.save(webhook);
        return request;
    }

    public List<Webhook> getAllWebhooks() {
        return webhookRepository.findAllWithRequests();
    }

    public Optional<Webhook> getWebhook(String path) {
        return webhookRepository.findByPath(path);
    }
}
