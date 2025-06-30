package com.example.webhooksite.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Webhook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String path;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "webhook", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<WebhookRequest> requests = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
