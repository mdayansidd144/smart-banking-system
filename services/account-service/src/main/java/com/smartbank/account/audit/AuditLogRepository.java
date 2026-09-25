package com.smartbank.account.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<AuditLog> findByActionOrderByCreatedAtDesc(AuditAction action, Pageable pageable);

    Page<AuditLog> findByAccountIdOrderByCreatedAtDesc(UUID accountId, Pageable pageable);

    Page<AuditLog> findByUsernameOrderByCreatedAtDesc(String username, Pageable pageable);

    Page<AuditLog> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime from, LocalDateTime to, Pageable pageable);

    // Use native SQL with explicit casts to avoid Postgres type inference errors
    @Query(value = """
        SELECT * FROM audit_logs
        WHERE (CAST(:action AS text) IS NULL OR action = CAST(:action AS text))
          AND (CAST(:accountId AS uuid) IS NULL OR account_id = CAST(:accountId AS uuid))
          AND (CAST(:username AS text) IS NULL OR username = CAST(:username AS text))
          AND (CAST(:from AS timestamp) IS NULL OR created_at >= CAST(:from AS timestamp))
          AND (CAST(:to AS timestamp) IS NULL OR created_at <= CAST(:to AS timestamp))
        ORDER BY created_at DESC
        """,
            countQuery = """
        SELECT COUNT(*) FROM audit_logs
        WHERE (CAST(:action AS text) IS NULL OR action = CAST(:action AS text))
          AND (CAST(:accountId AS uuid) IS NULL OR account_id = CAST(:accountId AS uuid))
          AND (CAST(:username AS text) IS NULL OR username = CAST(:username AS text))
          AND (CAST(:from AS timestamp) IS NULL OR created_at >= CAST(:from AS timestamp))
          AND (CAST(:to AS timestamp) IS NULL OR created_at <= CAST(:to AS timestamp))
        """,
            nativeQuery = true)
    Page<AuditLog> search(
            @Param("action") String action,
            @Param("accountId") String accountId,
            @Param("username") String username,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable);
}