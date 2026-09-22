package com.smartbank.account.repository;

import com.smartbank.account.entity.IdempotencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IdempotencyRepository extends JpaRepository<IdempotencyRecord, UUID> {
    Optional<IdempotencyRecord> findByIdempotencyKey(String idempotencyKey);
}