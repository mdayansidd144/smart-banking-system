package com.smartbank.account.service;
import com.smartbank.account.dto.CreateScheduledTransferRequest;
import com.smartbank.account.dto.ScheduledTransferResponse;
import com.smartbank.account.dto.TransferRequest;
import com.smartbank.account.dto.TransferResponse;
import com.smartbank.account.entity.ScheduledTransfer;
import com.smartbank.account.entity.ScheduledTransferStatus;
import com.smartbank.account.exception.TransferReversalException;
import com.smartbank.account.repository.ScheduledTransferRepository;
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
public class ScheduledTransferService {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTransferService.class);

    private final ScheduledTransferRepository repository;
    private final TransferService transferService;

    public ScheduledTransferService(ScheduledTransferRepository repository,
                                    TransferService transferService) {
        this.repository = repository;
        this.transferService = transferService;
    }

    @Transactional
    public ScheduledTransferResponse create(CreateScheduledTransferRequest request) {
        if (request.getFromAccountId().equals(request.getToAccountId())) {
            throw new TransferReversalException("Cannot schedule transfer to the same account");
        }

        // Validate cron
        CronExpression cron = CronExpression.parse(request.getCronExpression());

        ScheduledTransfer st = new ScheduledTransfer();
        st.setFromAccountId(request.getFromAccountId());
        st.setToAccountId(request.getToAccountId());
        st.setAmount(request.getAmount());
        st.setDescription(request.getDescription());
        st.setCronExpression(request.getCronExpression());
        st.setNextRunAt(request.getFirstRunAt());
        st.setStatus(ScheduledTransferStatus.ACTIVE);

        ScheduledTransfer saved = repository.save(st);
        log.info(" Scheduled transfer created: {}", saved.getId());
        return toResponse(saved);
    }

    public List<ScheduledTransferResponse> listByAccount(UUID accountId) {
        return repository.findByFromAccountIdOrderByCreatedAtDesc(accountId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<ScheduledTransferResponse> listAll() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ScheduledTransferResponse cancel(UUID id) {
        ScheduledTransfer st = repository.findById(id)
                .orElseThrow(() -> new TransferReversalException("Scheduled transfer not found"));
        st.setStatus(ScheduledTransferStatus.CANCELLED);
        return toResponse(repository.save(st));
    }

    @Transactional
    public int processDueTransfers() {
        LocalDateTime now = LocalDateTime.now();
        List<ScheduledTransfer> due = repository.findByStatusAndNextRunAtBefore(
                ScheduledTransferStatus.ACTIVE, now);

        if (due.isEmpty()) return 0;

        log.info(" Processing {} due scheduled transfers", due.size());
        int processed = 0;

        for (ScheduledTransfer st : due) {
            try {
                TransferRequest req = new TransferRequest();
                req.setFromAccountId(st.getFromAccountId());
                req.setToAccountId(st.getToAccountId());
                req.setAmount(st.getAmount());
                req.setDescription("[Scheduled] " + (st.getDescription() != null ? st.getDescription() : ""));

                TransferResponse resp = transferService.transfer(null, req);

                st.setLastRunAt(now);
                st.setLastTransferId(resp.getTransferId());
                st.setSuccessCount(st.getSuccessCount() + 1);
                st.setLastError(null);

                // Compute next run
                CronExpression cron = CronExpression.parse(st.getCronExpression());
                LocalDateTime next = cron.next(now);
                if (next == null) {
                    st.setStatus(ScheduledTransferStatus.COMPLETED);
                    st.setNextRunAt(now);   // no more runs
                } else {
                    st.setNextRunAt(next);
                }

                log.info(" Scheduled transfer {} executed → {}", st.getId(), resp.getTransferId());
                processed++;

            } catch (Exception e) {
                st.setLastRunAt(now);
                st.setFailureCount(st.getFailureCount() + 1);
                st.setLastError(e.getMessage());
                st.setStatus(ScheduledTransferStatus.PAUSED);   // pause on failure
                log.error(" Scheduled transfer {} failed: {}", st.getId(), e.getMessage());
            }

            repository.save(st);
        }

        return processed;
    }

    private ScheduledTransferResponse toResponse(ScheduledTransfer st) {
        return new ScheduledTransferResponse(
                st.getId(),
                st.getFromAccountId(),
                st.getToAccountId(),
                st.getAmount(),
                st.getDescription(),
                st.getCronExpression(),
                st.getStatus().name(),
                st.getNextRunAt(),
                st.getLastRunAt(),
                st.getLastTransferId(),
                st.getSuccessCount(),
                st.getFailureCount(),
                st.getLastError(),
                st.getCreatedAt()
        );
    }
}