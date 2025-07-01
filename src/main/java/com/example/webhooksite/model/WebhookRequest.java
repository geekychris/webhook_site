package com.example.webhooksite.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Data
@Getter
@Setter
public class WebhookRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Webhook webhook;

    private String method;
    
    @Column(length = 10000)
    private String body;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "webhook_request_headers")
    @MapKeyColumn(name = "header_name")
    @Column(name = "header_value")
    private Map<String, String> headers = new HashMap<>();

    private LocalDateTime receivedAt;

    @PrePersist
    protected void onCreate() {
        receivedAt = LocalDateTime.now();
    }
}
