package com.smartbank.identity.dto;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class KycResponse {

    private UUID id;
    private UUID userId;
    private String username;
    private String email;
    private String fullName;
    private LocalDate dateOfBirth;
    private String address;
    private String city;
    private String postalCode;
    private String status;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private String rejectionReason;
    private List<DocumentResponse> documents;

    public KycResponse() {}

    public KycResponse(UUID id, UUID userId, String username, String email,
                       String fullName, LocalDate dateOfBirth, String address,
                       String city, String postalCode, String status,
                       LocalDateTime submittedAt, LocalDateTime reviewedAt,
                       String rejectionReason, List<DocumentResponse> documents) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.city = city;
        this.postalCode = postalCode;
        this.status = status;
        this.submittedAt = submittedAt;
        this.reviewedAt = reviewedAt;
        this.rejectionReason = rejectionReason;
        this.documents = documents;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getAddress() { return address; }
    public String getCity() { return city; }
    public String getPostalCode() { return postalCode; }
    public String getStatus() { return status; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public String getRejectionReason() { return rejectionReason; }
    public List<DocumentResponse> getDocuments() { return documents; }

    // ---------- Nested DTO ----------

    public static class DocumentResponse {
        private UUID id;
        private String docType;
        private String documentNumber;
        private String documentUrl;
        private LocalDateTime uploadedAt;

        public DocumentResponse(UUID id, String docType, String documentNumber,
                                String documentUrl, LocalDateTime uploadedAt) {
            this.id = id;
            this.docType = docType;
            this.documentNumber = documentNumber;
            this.documentUrl = documentUrl;
            this.uploadedAt = uploadedAt;
        }

        public UUID getId() { return id; }
        public String getDocType() { return docType; }
        public String getDocumentNumber() { return documentNumber; }
        public String getDocumentUrl() { return documentUrl; }
        public LocalDateTime getUploadedAt() { return uploadedAt; }
    }
}