package com.smartbank.account.bill;
import com.smartbank.account.entity.Account;
import com.smartbank.account.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BillService {

    private static final Logger log = LoggerFactory.getLogger(BillService.class);

    private final ScheduledBillRepository billRepository;
    private final BillerRepository billerRepository;
    private final AccountRepository accountRepository;

    public BillService(ScheduledBillRepository billRepository,
                       BillerRepository billerRepository,
                       AccountRepository accountRepository) {
        this.billRepository = billRepository;
        this.billerRepository = billerRepository;
        this.accountRepository = accountRepository;
    }
    @Transactional
    public BillResponse create(BillRequest request) {
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found: " + request.getAccountId()));

        Biller biller = billerRepository.findById(request.getBillerId())
                .orElseThrow(() -> new RuntimeException("Biller not found: " + request.getBillerId()));

        // Validate cron
        CronExpression cron;
        try {
            cron = CronExpression.parse(request.getCronExpression());
        } catch (Exception e) {
            throw new RuntimeException("Invalid cron expression: " + e.getMessage());
        }

        // Compute next due date from now
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextRun = cron.next(now);
        if (nextRun == null) {
            throw new RuntimeException("Cron expression never produces a run time");
        }

        ScheduledBill bill = new ScheduledBill();
        bill.setAccountId(account.getId());
        bill.setBillerId(biller.getId());
        bill.setNickname(request.getNickname() != null && !request.getNickname().isBlank()
                ? request.getNickname()
                : biller.getName());
        bill.setAmount(request.getAmount());
        bill.setCronExpression(request.getCronExpression());
        bill.setNextDueAt(nextRun);
        bill.setStatus(BillStatus.ACTIVE);

        ScheduledBill saved = billRepository.save(bill);
        log.info("Created bill: {} for account {} — next due {}", saved.getId(), account.getId(), nextRun);

        return toResponse(saved, biller);
    }

    @Transactional(readOnly = true)
    public List<BillResponse> listAll() {
        return billRepository.findAll().stream()
                .map(b -> toResponse(b, billerRepository.findById(b.getBillerId()).orElse(null)))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BillResponse> listByAccount(UUID accountId) {
        return billRepository.findByAccountIdOrderByNextDueAtAsc(accountId).stream()
                .map(b -> toResponse(b, billerRepository.findById(b.getBillerId()).orElse(null)))
                .collect(Collectors.toList());
    }

    @Transactional
    public BillResponse pause(UUID id) {
        ScheduledBill bill = billRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bill not found: " + id));
        bill.setStatus(BillStatus.PAUSED);
        return toResponse(billRepository.save(bill), billerRepository.findById(bill.getBillerId()).orElse(null));
    }

    @Transactional
    public BillResponse resume(UUID id) {
        ScheduledBill bill = billRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bill not found: " + id));
        bill.setStatus(BillStatus.ACTIVE);
        return toResponse(billRepository.save(bill), billerRepository.findById(bill.getBillerId()).orElse(null));
    }

    @Transactional
    public void cancel(UUID id) {
        ScheduledBill bill = billRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bill not found: " + id));
        bill.setStatus(BillStatus.CANCELLED);
        billRepository.save(bill);
    }


    private BillResponse toResponse(ScheduledBill b, Biller biller) {
        return new BillResponse(
                b.getId(),
                b.getAccountId(),
                b.getBillerId(),
                biller != null ? biller.getName() : "Unknown",
                biller != null ? biller.getCategory() : "OTHER",
                b.getNickname(),
                b.getAmount(),
                b.getCronExpression(),
                b.getNextDueAt(),
                b.getLastPaidAt(),
                b.getLastTransferId(),
                b.getStatus().name(),
                b.getSuccessCount(),
                b.getFailureCount(),
                b.getLastError(),
                b.getCreatedAt()
        );
    }
}