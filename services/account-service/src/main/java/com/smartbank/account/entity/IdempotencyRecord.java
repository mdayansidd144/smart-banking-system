package com.smartbank.account.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
@Entity
@Table(name = "idempotency_records",
        uniqueConstraints = @UniqueConstraint(columnNames = "idempotency_key"))
public class IdempotencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 100)
    private String idempotencyKey;
    @Column(name = "response_json", columnDefinition = "TEXT")
    private String responseJson;
    @Column(nullable = false)
    private Integer responseStatus;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public String getResponseJson() { return responseJson; }
    public void setResponseJson(String responseJson) { this.responseJson = responseJson; }
    public Integer getResponseStatus() { return responseStatus; }
    public void setResponseStatus(Integer responseStatus) { this.responseStatus = responseStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}