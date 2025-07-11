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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import com.vaadin.flow.shared.Registration;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

@Service
@Transactional
@org.springframework.scheduling.annotation.EnableAsync
public class WebhookService {
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<Consumer<WebhookRequest>>> listeners = new ConcurrentHashMap<>();
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
    public WebhookRequest recordWebhookRequest(String path, String method, String body, Map<String, String> headers, String remoteHost, String serverHost) {
        Webhook webhook = webhookRepository.findByPath(path)
            .orElseThrow(() -> new IllegalArgumentException("Webhook not found"));

        WebhookRequest request = new WebhookRequest();
        request.setWebhook(webhook);
        request.setMethod(method);
        request.setBody(body);
        request.setHeaders(headers);
        request.setRemoteHost(remoteHost);
        request.setServerHost(serverHost);

        webhook.getRequests().add(request);
        webhookRepository.save(webhook);
        // Notify listeners
        if (listeners.containsKey(path)) {
            listeners.get(path).forEach(listener -> listener.accept(request));
        }
        
        return request;
    }

    public Registration registerWebhookListener(String path, Consumer<WebhookRequest> listener) {
        listeners.computeIfAbsent(path, k -> new CopyOnWriteArrayList<>()).add(listener);
        return () -> listeners.get(path).remove(listener);
    }

    public List<Webhook> getAllWebhooks() {
        return webhookRepository.findAllWithRequests();
    }

    public Optional<Webhook> getWebhook(String path) {
        return webhookRepository.findByPath(path);
    }
}
