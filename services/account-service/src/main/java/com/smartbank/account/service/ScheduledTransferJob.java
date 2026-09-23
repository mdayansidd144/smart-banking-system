package com.smartbank.account.service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTransferJob {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTransferJob.class);

    private final ScheduledTransferService service;

    public ScheduledTransferJob(ScheduledTransferService service) {
        this.service = service;
    }

    @Scheduled(fixedDelay = 60000, initialDelay = 30000)
    public void run() {
        try {
            int count = service.processDueTransfers();
            if (count > 0) {
                log.info(" Processed {} scheduled transfers", count);
            }
        } catch (Exception e) {
            log.error(" Scheduler error: {}", e.getMessage());
        }
    }
}