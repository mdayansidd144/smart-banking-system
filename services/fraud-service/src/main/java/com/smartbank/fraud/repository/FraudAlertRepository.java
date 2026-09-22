package com.smartbank.fraud.repository;
import com.smartbank.fraud.entity.FraudAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
@Repository
public interface FraudAlertRepository extends JpaRepository<FraudAlert, UUID> {
    List<FraudAlert> findByAccountIdOrderByCreatedAtDesc(UUID accountId);
    List<FraudAlert> findBySeverityOrderByCreatedAtDesc(String severity);
    List<FraudAlert> findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime after);
    List<FraudAlert> findAllByOrderByCreatedAtDesc();
}