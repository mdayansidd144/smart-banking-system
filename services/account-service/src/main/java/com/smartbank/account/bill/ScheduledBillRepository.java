package com.smartbank.account.bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ScheduledBillRepository extends JpaRepository<ScheduledBill, UUID> {

    List<ScheduledBill> findByAccountIdOrderByNextDueAtAsc(UUID accountId);

    List<ScheduledBill> findByStatusOrderByNextDueAtAsc(BillStatus status);
    @Query("SELECT b FROM ScheduledBill b WHERE b.status = 'ACTIVE' " +
            "AND b.nextDueAt <= :cutoff")
    List<ScheduledBill> findDueBefore(@Param("cutoff") LocalDateTime cutoff);

    @Query("SELECT b FROM ScheduledBill b WHERE b.status = 'ACTIVE' " +
            "AND b.nextDueAt >= :from AND b.nextDueAt < :to")
    List<ScheduledBill> findDueBetween(@Param("from") LocalDateTime from,
                                       @Param("to") LocalDateTime to);
}