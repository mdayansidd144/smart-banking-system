package com.smartbank.fraud.ai.tools;
import com.smartbank.fraud.entity.FraudAlert;
import com.smartbank.fraud.repository.FraudAlertRepository;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class CreditScoringTools {

    private final RestTemplate restTemplate;
    private final FraudAlertRepository fraudAlertRepository;

    public CreditScoringTools(FraudAlertRepository fraudAlertRepository) {
        this.restTemplate = new RestTemplate();
        this.fraudAlertRepository = fraudAlertRepository;
    }

    @Tool("Get account balance for a given account ID")
    public double getAccountBalance(@P("account UUID") String accountId) {
        try {
            String url = "http://account-service:8080/api/v1/accounts/" + accountId;
            Map<?, ?> resp = restTemplate.getForObject(url, Map.class);
            if (resp != null && resp.get("balance") != null) {
                return Double.parseDouble(resp.get("balance").toString());
            }
        } catch (Exception e) {
            return -1;
        }
        return -1;
    }

    @Tool("Get account creation date for a given account ID (YYYY-MM-DD)")
    public String getAccountAge(@P("account UUID") String accountId) {
        try {
            String url = "http://account-service:8080/api/v1/accounts/" + accountId;
            Map<?, ?> resp = restTemplate.getForObject(url, Map.class);
            if (resp != null && resp.get("createdAt") != null) {
                return resp.get("createdAt").toString();
            }
        } catch (Exception e) {
            return "unknown";
        }
        return "unknown";
    }

    @Tool("Get the total number of transactions for a given account")
    public int getTransactionCount(@P("account UUID") String accountId) {
        try {
            String url = "http://account-service:8080/api/v1/accounts/" + accountId + "/transactions";
            List<?> list = restTemplate.getForObject(url, List.class);
            return list != null ? list.size() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    @Tool("Get the sum of all deposits for a given account")
    public double getTotalDeposits(@P("account UUID") String accountId) {
        try {
            String url = "http://account-service:8080/api/v1/accounts/" + accountId + "/transactions";
            List<Map<String, Object>> list = restTemplate.getForObject(url, List.class);
            if (list == null) return 0;
            return list.stream()
                    .filter(t -> "DEPOSIT".equals(t.get("type")))
                    .map(t -> new BigDecimal(t.get("amount").toString()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .doubleValue();
        } catch (Exception e) {
            return 0;
        }
    }

    @Tool("Get the sum of all withdrawals for a given account")
    public double getTotalWithdrawals(@P("account UUID") String accountId) {
        try {
            String url = "http://account-service:8080/api/v1/accounts/" + accountId + "/transactions";
            List<Map<String, Object>> list = restTemplate.getForObject(url, List.class);
            if (list == null) return 0;
            return list.stream()
                    .filter(t -> "WITHDRAWAL".equals(t.get("type")))
                    .map(t -> new BigDecimal(t.get("amount").toString()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .doubleValue();
        } catch (Exception e) {
            return 0;
        }
    }

    @Tool("Get the number of fraud alerts associated with a given account")
    public int getFraudAlertCount(@P("account UUID") String accountId) {
        try {
            UUID id = UUID.fromString(accountId);
            List<FraudAlert> alerts = fraudAlertRepository.findByAccountIdOrderByCreatedAtDesc(id);
            return alerts.size();
        } catch (Exception e) {
            return 0;
        }
    }
}