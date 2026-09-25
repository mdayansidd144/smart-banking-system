package com.smartbank.identity.repository;
import com.smartbank.identity.entity.KycStatus;
import com.smartbank.identity.entity.KycSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface KycSubmissionRepository extends JpaRepository<KycSubmission, UUID> {

    Optional<KycSubmission> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    List<KycSubmission> findByStatusOrderBySubmittedAtAsc(KycStatus status);

    List<KycSubmission> findAllByOrderBySubmittedAtDesc();
}