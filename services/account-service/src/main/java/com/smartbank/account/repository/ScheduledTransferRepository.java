package com.smartbank.account.repository;

import com.smartbank.account.entity.ScheduledTransfer;
import com.smartbank.account.entity.ScheduledTransferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ScheduledTransferRepository extends JpaRepository<ScheduledTransfer, UUID> {

    List<ScheduledTransfer> findByStatusAndNextRunAtBefore(
            ScheduledTransferStatus status, LocalDateTime cutoff);

    List<ScheduledTransfer> findByFromAccountIdOrderByCreatedAtDesc(UUID fromAccountId);
}