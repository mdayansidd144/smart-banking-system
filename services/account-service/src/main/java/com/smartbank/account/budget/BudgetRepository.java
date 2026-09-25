package com.smartbank.account.budget;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, UUID> {

    List<Budget> findByUserIdOrderByCategoryAsc(String userId);

    Optional<Budget> findByUserIdAndCategory(String userId, String category);

    boolean existsByUserIdAndCategory(String userId, String category);

    List<Budget> findAll();
}