package com.smartbank.fraud.repository;
import com.smartbank.fraud.entity.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, UUID> {

    List<LoanApplication> findByAccountIdOrderByCreatedAtDesc(UUID accountId);

    List<LoanApplication> findAllByOrderByCreatedAtDesc();
}