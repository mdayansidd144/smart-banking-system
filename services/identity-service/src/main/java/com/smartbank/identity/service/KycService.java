package com.smartbank.identity.service;
import com.smartbank.identity.dto.KycRequest;
import com.smartbank.identity.dto.KycResponse;
import com.smartbank.identity.entity.KycDocument;
import com.smartbank.identity.entity.KycStatus;
import com.smartbank.identity.entity.KycSubmission;
import com.smartbank.identity.entity.User;
import com.smartbank.identity.repository.KycDocumentRepository;
import com.smartbank.identity.repository.KycSubmissionRepository;
import com.smartbank.identity.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class KycService {

    private static final Logger log = LoggerFactory.getLogger(KycService.class);

    private final KycSubmissionRepository submissionRepository;
    private final KycDocumentRepository documentRepository;
    private final UserRepository userRepository;

    public KycService(KycSubmissionRepository submissionRepository,
                      KycDocumentRepository documentRepository,
                      UserRepository userRepository) {
        this.submissionRepository = submissionRepository;
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
    }

    // =========================================================
    // Submit KYC
    // =========================================================

    @Transactional
    public KycResponse submit(UUID userId, KycRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if a submission already exists
        KycSubmission existing = submissionRepository.findByUserId(userId).orElse(null);
        KycSubmission submission;

        if (existing != null) {
            if (existing.getStatus() == KycStatus.PENDING) {
                throw new RuntimeException("KYC already submitted and pending review");
            }
            if (existing.getStatus() == KycStatus.APPROVED) {
                throw new RuntimeException("KYC already approved");
            }
            // If REJECTED, allow resubmission
            submission = existing;
            submission.setFullName(request.getFullName());
            submission.setDateOfBirth(request.getDateOfBirth());
            submission.setAddress(request.getAddress());
            submission.setCity(request.getCity());
            submission.setPostalCode(request.getPostalCode());
            submission.setStatus(KycStatus.PENDING);
            submission.setSubmittedAt(LocalDateTime.now());
            submission.setReviewedAt(null);
            submission.setReviewedBy(null);
            submission.setRejectionReason(null);
            documentRepository.deleteBySubmissionId(submission.getId());
        } else {
            submission = new KycSubmission();
            submission.setUserId(userId);
            submission.setFullName(request.getFullName());
            submission.setDateOfBirth(request.getDateOfBirth());
            submission.setAddress(request.getAddress());
            submission.setCity(request.getCity());
            submission.setPostalCode(request.getPostalCode());
            submission.setStatus(KycStatus.PENDING);
        }

        KycSubmission saved = submissionRepository.save(submission);

        // Create documents (PAN + Aadhaar)
        KycDocument pan = new KycDocument();
        pan.setSubmissionId(saved.getId());
        pan.setDocType("PAN");
        pan.setDocumentNumber(request.getPanNumber().toUpperCase());
        documentRepository.save(pan);

        KycDocument aadhaar = new KycDocument();
        aadhaar.setSubmissionId(saved.getId());
        aadhaar.setDocType("AADHAAR");
        aadhaar.setDocumentNumber(request.getAadhaarNumber());
        documentRepository.save(aadhaar);

        log.info("KYC submitted for user {} (submission {})", userId, saved.getId());
        return toResponse(saved, user);
    }

    // =========================================================
    // Get status
    // =========================================================

    @Transactional(readOnly = true)
    public KycResponse getForUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        KycSubmission submission = submissionRepository.findByUserId(userId).orElse(null);
        if (submission == null) {
            // Return a NOT_STARTED stub
            return new KycResponse(
                    null, userId, user.getUsername(), user.getEmail(),
                    null, null, null, null, null,
                    KycStatus.NOT_STARTED.name(),
                    null, null, null, List.of()
            );
        }
        return toResponse(submission, user);
    }

    // =========================================================
    // Admin — list pending
    // =========================================================

    @Transactional(readOnly = true)
    public List<KycResponse> listPending() {
        return submissionRepository.findByStatusOrderBySubmittedAtAsc(KycStatus.PENDING)
                .stream()
                .map(this::toResponseWithUser)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<KycResponse> listAll() {
        return submissionRepository.findAllByOrderBySubmittedAtDesc()
                .stream()
                .map(this::toResponseWithUser)
                .collect(Collectors.toList());
    }

    // =========================================================
    // Admin — approve
    // =========================================================

    @Transactional
    public KycResponse approve(UUID submissionId, UUID adminId) {
        KycSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));

        if (submission.getStatus() != KycStatus.PENDING) {
            throw new RuntimeException("Only PENDING submissions can be approved");
        }

        submission.setStatus(KycStatus.APPROVED);
        submission.setReviewedAt(LocalDateTime.now());
        submission.setReviewedBy(adminId);
        submission.setRejectionReason(null);
        submissionRepository.save(submission);

        // Mark user as KYC-verified
        User user = userRepository.findById(submission.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setKycVerified(true);
        userRepository.save(user);

        log.info("KYC approved for user {} by admin {}", user.getId(), adminId);
        return toResponse(submission, user);
    }

    // =========================================================
    // Admin — reject
    // =========================================================

    @Transactional
    public KycResponse reject(UUID submissionId, UUID adminId, String reason) {
        KycSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));

        if (submission.getStatus() != KycStatus.PENDING) {
            throw new RuntimeException("Only PENDING submissions can be rejected");
        }

        submission.setStatus(KycStatus.REJECTED);
        submission.setReviewedAt(LocalDateTime.now());
        submission.setReviewedBy(adminId);
        submission.setRejectionReason(reason != null ? reason : "No reason provided");
        submissionRepository.save(submission);

        User user = userRepository.findById(submission.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setKycVerified(false);
        userRepository.save(user);

        log.info("KYC rejected for user {} by admin {}: {}", user.getId(), adminId, reason);
        return toResponse(submission, user);
    }

    // =========================================================
    // Mappers
    // =========================================================

    private KycResponse toResponse(KycSubmission s, User user) {
        List<KycResponse.DocumentResponse> docs = documentRepository
                .findBySubmissionId(s.getId())
                .stream()
                .map(d -> new KycResponse.DocumentResponse(
                        d.getId(), d.getDocType(), d.getDocumentNumber(),
                        d.getDocumentUrl(), d.getUploadedAt()
                ))
                .collect(Collectors.toList());

        return new KycResponse(
                s.getId(),
                s.getUserId(),
                user != null ? user.getUsername() : null,
                user != null ? user.getEmail() : null,
                s.getFullName(),
                s.getDateOfBirth(),
                s.getAddress(),
                s.getCity(),
                s.getPostalCode(),
                s.getStatus().name(),
                s.getSubmittedAt(),
                s.getReviewedAt(),
                s.getRejectionReason(),
                docs
        );
    }

    private KycResponse toResponseWithUser(KycSubmission s) {
        User user = userRepository.findById(s.getUserId()).orElse(null);
        return toResponse(s, user);
    }
}