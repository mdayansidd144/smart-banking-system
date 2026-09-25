package com.smartbank.account.budget;
import com.smartbank.account.event.EventPublisher;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@Component
public class BudgetAlertJob {

    private static final Logger log = LoggerFactory.getLogger(BudgetAlertJob.class);

    private final BudgetRepository budgetRepository;
    private final BudgetService budgetService;
    private final EventPublisher eventPublisher;

    public BudgetAlertJob(BudgetRepository budgetRepository,
                          BudgetService budgetService,
                          EventPublisher eventPublisher) {
        this.budgetRepository = budgetRepository;
        this.budgetService = budgetService;
        this.eventPublisher = eventPublisher;
    }

    @PostConstruct
    public void init() {
        log.info("=== BudgetAlertJob BEAN CREATED — scheduler should be active ===");
    }

    /**
     * Runs every 5 minutes for testing (initial delay 30s).
     * In production, change to 60 * 60 * 1000 (1 hour).
     */
    @Scheduled(fixedDelay = 5 * 60 * 1000, initialDelay = 30 * 1000)
    @Transactional
    public void checkBudgets() {
        log.info("BudgetAlertJob TICK — starting check");
        try {
            String currentMonth = YearMonth.now().toString();

            List<Budget> all = budgetRepository.findAll();
            log.info("BudgetAlertJob: found {} budgets for month {}", all.size(), currentMonth);

            if (all.isEmpty()) {
                log.info("BudgetAlertJob: no budgets to check — exiting");
                return;
            }

            Map<String, BigDecimal> spends = budgetService.computeMonthlySpendByCategory();

            for (Budget b : all) {
                // Reset flags on month change
                if (!currentMonth.equals(b.getLastResetMonth())) {
                    b.setAlertSent80(false);
                    b.setAlertSent100(false);
                    b.setLastResetMonth(currentMonth);
                }

                BigDecimal spend = spends.getOrDefault(b.getCategory(), BigDecimal.ZERO);
                BigDecimal limit = b.getMonthlyLimit();
                if (limit.signum() <= 0) continue;

                double pct = spend.divide(limit, 4, RoundingMode.HALF_UP).doubleValue() * 100;

                if (pct >= 100 && !b.isAlertSent100()) {
                    eventPublisher.publishBudgetAlert(new BudgetAlertEvent(
                            b.getId(), b.getUserId(), b.getCategory(),
                            limit, spend, Math.round(pct * 10.0) / 10.0, "EXCEEDED"
                    ));
                    b.setAlertSent100(true);
                    b.setAlertSent80(true);
                    log.warn("Budget EXCEEDED: {} ({}% of ₹{})", b.getCategory(), pct, limit);
                } else if (pct >= 80 && !b.isAlertSent80()) {
                    eventPublisher.publishBudgetAlert(new BudgetAlertEvent(
                            b.getId(), b.getUserId(), b.getCategory(),
                            limit, spend, Math.round(pct * 10.0) / 10.0, "WARNING"
                    ));
                    b.setAlertSent80(true);
                    log.warn("Budget WARNING: {} ({}% of ₹{})", b.getCategory(), pct, limit);
                }

                budgetRepository.save(b);
            }

            log.info("BudgetAlertJob: check complete");

        } catch (Exception e) {
            log.error("Budget alert job failed: {}", e.getMessage(), e);
        }
    }
}