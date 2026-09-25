package com.smartbank.identity.repository;
import com.smartbank.identity.entity.KycDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface KycDocumentRepository extends JpaRepository<KycDocument, UUID> {

    List<KycDocument> findBySubmissionId(UUID submissionId);

    void deleteBySubmissionId(UUID submissionId);
}