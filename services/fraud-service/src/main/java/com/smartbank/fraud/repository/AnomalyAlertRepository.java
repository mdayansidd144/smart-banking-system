package com.smartbank.fraud.repository;
import com.smartbank.fraud.entity.AnomalyAlert;
import com.smartbank.fraud.entity.AnomalySeverity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AnomalyAlertRepository extends JpaRepository<AnomalyAlert, UUID> {

    List<AnomalyAlert> findByAccountIdOrderByCreatedAtDesc(UUID accountId);

    List<AnomalyAlert> findAllByOrderByCreatedAtDesc();

    List<AnomalyAlert> findBySeverityOrderByCreatedAtDesc(AnomalySeverity severity);

    List<AnomalyAlert> findByStatusOrderByCreatedAtDesc(AnomalyAlert.AlertStatus status);

    /**
     * Count transactions for an account within a time window.
     * Used by RAPID_VELOCITY rule.
     */
    @Query("SELECT COUNT(a) FROM AnomalyAlert a WHERE a.accountId = :accountId " +
            "AND a.createdAt >= :since")
    long countRecentByAccount(@Param("accountId") UUID accountId,
                              @Param("since") LocalDateTime since);
    @Query("SELECT COALESCE(SUM(a.amount), 0) FROM AnomalyAlert a " +
            "WHERE a.accountId = :accountId AND a.createdAt >= :since")
    java.math.BigDecimal sumRecentByAccount(@Param("accountId") UUID accountId,
                                            @Param("since") LocalDateTime since);
}