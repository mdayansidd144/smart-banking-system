package com.smartbank.fraud.ai.tools;
import com.smartbank.fraud.entity.AnomalyAlert;
import com.smartbank.fraud.repository.AnomalyAlertRepository;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class BankingAgentTools {

    private static final Logger log = LoggerFactory.getLogger(BankingAgentTools.class);
    private static final String ACCOUNT_BASE = "http://account-service:8080/api/v1";

    private final RestTemplate restTemplate;
    private final AnomalyAlertRepository anomalyRepository;

    public BankingAgentTools(AnomalyAlertRepository anomalyRepository) {
        this.restTemplate = new RestTemplate();
        this.anomalyRepository = anomalyRepository;
    }

    @Tool("List all customer accounts in the bank. Returns array with id, ownerName, accountNumber, balance, currency, status.")
    @SuppressWarnings("unchecked")
    public String listAccounts() {
        try {
            List<Map<String, Object>> accounts =
                    restTemplate.getForObject(ACCOUNT_BASE + "/accounts", List.class);
            if (accounts == null || accounts.isEmpty()) return "No accounts found.";
            StringBuilder sb = new StringBuilder("Accounts:\n");
            for (Map<String, Object> a : accounts) {
                sb.append(String.format("- %s | %s | %s %s | %s | id=%s%n",
                        a.get("ownerName"),
                        a.get("accountNumber"),
                        a.get("currency"),
                        a.get("balance"),
                        a.get("status"),
                        a.get("id")));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("listAccounts failed: {}", e.getMessage());
            return "Error fetching accounts: " + e.getMessage();
        }
    }

    @Tool("Get details of a specific account by its UUID. Returns owner name, balance, currency, status.")
    @SuppressWarnings("unchecked")
    public String getAccountById(@P("account UUID") String accountId) {
        try {
            Map<String, Object> a = restTemplate.getForObject(
                    ACCOUNT_BASE + "/accounts/" + accountId, Map.class);
            if (a == null) return "Account not found: " + accountId;
            return String.format(
                    "Account %s: owner=%s, balance=%s %s, status=%s",
                    a.get("accountNumber"), a.get("ownerName"),
                    a.get("currency"), a.get("balance"), a.get("status"));
        } catch (Exception e) {
            return "Error: account not found or ID invalid (" + accountId + ")";
        }
    }

    @Tool("Find accounts by owner name. Case-insensitive partial match.")
    @SuppressWarnings("unchecked")
    public String searchAccountsByName(@P("partial owner name") String name) {
        try {
            List<Map<String, Object>> accounts =
                    restTemplate.getForObject(ACCOUNT_BASE + "/accounts", List.class);
            if (accounts == null) return "No accounts.";
            StringBuilder sb = new StringBuilder();
            String lower = name.toLowerCase();
            for (Map<String, Object> a : accounts) {
                String owner = String.valueOf(a.get("ownerName")).toLowerCase();
                if (owner.contains(lower)) {
                    sb.append(String.format("- %s | %s | %s %s%n",
                            a.get("ownerName"), a.get("accountNumber"),
                            a.get("currency"), a.get("balance")));
                }
            }
            return sb.isEmpty() ? "No accounts matching '" + name + "'." : sb.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Tool("Get recent transactions for an account by its UUID.")
    @SuppressWarnings("unchecked")
    public String getTransactions(@P("account UUID") String accountId) {
        try {
            List<Map<String, Object>> txs = restTemplate.getForObject(
                    ACCOUNT_BASE + "/accounts/" + accountId + "/transactions", List.class);
            if (txs == null || txs.isEmpty()) return "No transactions for this account.";
            StringBuilder sb = new StringBuilder();
            int count = 0;
            for (Map<String, Object> t : txs) {
                if (count++ >= 10) break;
                sb.append(String.format("- %s %s %s | %s%n",
                        t.get("type"), t.get("amount"), t.get("currency"),
                        t.get("description")));
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error fetching transactions: " + e.getMessage();
        }
    }

    @Tool("List all anomaly alerts detected by the fraud system. Returns rule, severity, amount, reason.")
    public String listAnomalies() {
        try {
            List<AnomalyAlert> alerts = anomalyRepository.findAllByOrderByCreatedAtDesc();
            if (alerts.isEmpty()) return "No anomalies detected.";
            StringBuilder sb = new StringBuilder();
            int count = 0;
            for (AnomalyAlert a : alerts) {
                if (count++ >= 10) break;
                sb.append(String.format("- [%s] %s | %s %s | %s%n",
                        a.getSeverity(), a.getRuleTriggered(),
                        a.getEventType(), a.getAmount(), a.getReason()));
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error fetching anomalies: " + e.getMessage();
        }
    }

    @Tool("List OPEN anomaly alerts only (status = OPEN).")
    public String listOpenAnomalies() {
        try {
            List<AnomalyAlert> alerts = anomalyRepository
                    .findByStatusOrderByCreatedAtDesc(AnomalyAlert.AlertStatus.OPEN);
            if (alerts.isEmpty()) return "No open anomalies.";
            StringBuilder sb = new StringBuilder();
            for (AnomalyAlert a : alerts) {
                sb.append(String.format("- [%s] %s | %s %s | %s%n",
                        a.getSeverity(), a.getRuleTriggered(),
                        a.getEventType(), a.getAmount(), a.getReason()));
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Tool("Get total balance across all accounts in the bank.")
    @SuppressWarnings("unchecked")
    public String getTotalBankBalance() {
        try {
            List<Map<String, Object>> accounts =
                    restTemplate.getForObject(ACCOUNT_BASE + "/accounts", List.class);
            if (accounts == null) return "No accounts.";
            double total = 0;
            for (Map<String, Object> a : accounts) {
                Object bal = a.get("balance");
                if (bal != null) total += Double.parseDouble(bal.toString());
            }
            return String.format("Total bank balance: ₹%.2f across %d accounts", total, accounts.size());
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Tool("Count anomalies by severity (LOW, MEDIUM, HIGH, CRITICAL).")
    public String countAnomaliesBySeverity() {
        try {
            List<AnomalyAlert> all = anomalyRepository.findAll();
            long low = all.stream().filter(a -> a.getSeverity().name().equals("LOW")).count();
            long med = all.stream().filter(a -> a.getSeverity().name().equals("MEDIUM")).count();
            long high = all.stream().filter(a -> a.getSeverity().name().equals("HIGH")).count();
            long crit = all.stream().filter(a -> a.getSeverity().name().equals("CRITICAL")).count();
            return String.format("Anomaly counts — LOW: %d, MEDIUM: %d, HIGH: %d, CRITICAL: %d (total: %d)",
                    low, med, high, crit, all.size());
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}