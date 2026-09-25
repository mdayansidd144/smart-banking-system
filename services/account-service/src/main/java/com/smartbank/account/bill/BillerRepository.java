package com.smartbank.account.bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BillerRepository extends JpaRepository<Biller, UUID> {

    List<Biller> findAllByOrderByNameAsc();

    Optional<Biller> findByCode(String code);

    boolean existsByCode(String code);
}