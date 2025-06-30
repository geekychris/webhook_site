package com.example.webhooksite.repository;

import com.example.webhooksite.model.Webhook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface WebhookRepository extends JpaRepository<Webhook, Long> {
    @Query("SELECT w FROM Webhook w LEFT JOIN FETCH w.requests WHERE w.path = :path")
    Optional<Webhook> findByPath(String path);
    
    boolean existsByPath(String path);
    
    @Query("SELECT w FROM Webhook w LEFT JOIN FETCH w.requests")
    List<Webhook> findAllWithRequests();
}
