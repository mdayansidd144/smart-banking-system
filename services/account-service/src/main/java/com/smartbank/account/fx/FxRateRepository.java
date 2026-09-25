package com.smartbank.account.fx;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FxRateRepository extends JpaRepository<FxRate, UUID> {

    Optional<FxRate> findByBaseCurrencyAndTargetCurrency(String base, String target);

    List<FxRate> findByBaseCurrency(String base);

    @Query("SELECT MAX(r.fetchedAt) FROM FxRate r")
    LocalDateTime findLatestFetchTime();
}