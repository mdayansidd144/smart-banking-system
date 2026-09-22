package com.smartbank.fraud.ai.tools;
import com.smartbank.fraud.entity.FraudAlert;
import com.smartbank.fraud.repository.FraudAlertRepository;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class FraudQueryTools {
    private final FraudAlertRepository repository;
    public FraudQueryTools(FraudAlertRepository repository) {
        this.repository = repository;
    }
    @Tool("Get the total number of fraud alerts in the system")
    public long countAllAlerts() {
        return repository.count();
    }

    @Tool("Get all fraud alerts, newest first. Limit to at most 20 results.")
    public List<String> getAllAlerts() {
        return repository.findAllByOrderByCreatedAtDesc().stream()
                .limit(20)
                .map(this::format)
                .collect(Collectors.toList());
    }

    @Tool("Get fraud alerts with a specific severity: LOW, MEDIUM, HIGH, or CRITICAL")
    public List<String> getAlertsBySeverity(@P("severity level") String severity) {
        return repository.findBySeverityOrderByCreatedAtDesc(severity.toUpperCase())
                .stream()
                .limit(20)
                .map(this::format)
                .collect(Collectors.toList());
    }

    @Tool("Get fraud alerts for a specific account, given its account ID (UUID)")
    public List<String> getAlertsForAccount(@P("account UUID") String accountId) {
        try {
            UUID id = UUID.fromString(accountId);
            return repository.findByAccountIdOrderByCreatedAtDesc(id).stream()
                    .map(this::format)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            return List.of("Invalid UUID: " + accountId);
        }
    }

    @Tool("Get fraud alerts with an amount greater than or equal to the given value (in INR)")
    public List<String> getAlertsAboveAmount(@P("minimum amount") double minAmount) {
        BigDecimal threshold = BigDecimal.valueOf(minAmount);
        return repository.findAllByOrderByCreatedAtDesc().stream()
                .filter(a -> a.getAmount().compareTo(threshold) >= 0)
                .limit(20)
                .map(this::format)
                .collect(Collectors.toList());
    }

    @Tool("Get fraud alerts created after a given number of hours ago")
    public List<String> getRecentAlerts(@P("number of hours ago") int hoursAgo) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(hoursAgo);
        return repository.findByCreatedAtAfterOrderByCreatedAtDesc(cutoff).stream()
                .map(this::format)
                .collect(Collectors.toList());
    }

    private String format(FraudAlert alert) {
        return String.format(
                "[%s] %s | Amount: ₹%s | Account: %s | Score: %d | Reason: %s | At: %s",
                alert.getSeverity(),
                alert.getEventType(),
                alert.getAmount(),
                alert.getAccountId(),
                alert.getRiskScore(),
                alert.getReason(),
                alert.getCreatedAt()
        );
    }
}