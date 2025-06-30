package com.example.webhooksite.controller;

import com.example.webhooksite.service.WebhookService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/webhook")
public class WebhookController {
    private final WebhookService webhookService;

    public WebhookController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @RequestMapping(value = "/{path}/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, 
        RequestMethod.DELETE, RequestMethod.PATCH, RequestMethod.HEAD, RequestMethod.OPTIONS})
    public ResponseEntity<String> handleWebhook(@PathVariable String path, HttpServletRequest request) throws IOException {
        // Extract headers
        Map<String, String> headers = new HashMap<>();
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> 
            headers.put(headerName, request.getHeader(headerName)));

        // Extract body
        String body = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

        webhookService.recordWebhookRequest(path, request.getMethod(), body, headers);
        
        return ResponseEntity.ok("Webhook received");
    }
}
