package com.smartbank.fraud.service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartbank.fraud.ai.CreditScoringAssistant;
import com.smartbank.fraud.dto.LoanApplicationRequest;
import com.smartbank.fraud.dto.LoanApplicationResponse;
import com.smartbank.fraud.entity.LoanApplication;
import com.smartbank.fraud.repository.LoanApplicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
@Service
public class LoanService {

    private static final Logger log = LoggerFactory.getLogger(LoanService.class);

    private final LoanApplicationRepository repository;
    private final CreditScoringAssistant assistant;
    private final ObjectMapper objectMapper;

    public LoanService(LoanApplicationRepository repository,
                       CreditScoringAssistant assistant,
                       ObjectMapper objectMapper) {
        this.repository = repository;
        this.assistant = assistant;
        this.objectMapper = objectMapper;
    }

    // =========================================================
    // Apply for a loan
    // =========================================================
    @Transactional
    public LoanApplicationResponse apply(LoanApplicationRequest request) {
        String summary = String.format(
                "Customer with account ID %s is applying for a loan of %s %s for %d months. Purpose: %s",
                 request.getAccountId(),
                "INR",
                request.getAmount(),
                request.getTermMonths(),
                request.getPurpose() != null ? request.getPurpose() : "Not specified"
        );

        log.info(" AI evaluating loan: {}", summary);

        // ---- Call LLM ----
        String aiResponse;
        try {
            aiResponse = assistant.evaluate(summary);
        } catch (Exception e) {
            log.error(" LLM call threw exception: {}", e.getMessage(), e);
            throw new RuntimeException("AI service error: " + e.getMessage(), e);
        }

        // ---- Debug: log what came back ----
        if (aiResponse == null) {
            log.error(" AI response is NULL");
            throw new RuntimeException("AI returned null response");
        }
        log.info(" RAW AI RESPONSE length = {}", aiResponse.length());
        log.info(" RAW AI RESPONSE:\n=====\n{}\n=====", aiResponse);

        if (aiResponse.isBlank()) {
            log.error(" AI response is BLANK");
            throw new RuntimeException("AI returned empty response. Check Ollama logs.");
        }

        // ---- Extract JSON ----
        String clean;
        try {
            clean = extractJson(aiResponse);
        } catch (Exception e) {
            log.error(" Failed to extract JSON from response: {}", e.getMessage());
            throw new RuntimeException("AI response is not JSON: " + aiResponse);
        }
        log.info(" Extracted JSON:\n{}", clean);

        // ---- Parse JSON ----
        JsonNode node;
        try {
            node = objectMapper.readTree(clean);
        } catch (Exception e) {
            log.error(" Failed to parse AI response. Raw was:\n{}", aiResponse);
            throw new RuntimeException("AI returned invalid JSON: " + aiResponse);
        }

        // ---- Validate fields ----
        requireField(node, "decision");
        requireField(node, "riskScore");
        requireField(node, "interestRate");
        requireField(node, "approvedAmount");
        requireField(node, "reasoning");

        // ---- Build entity ----
        LoanApplication app = new LoanApplication();
        app.setAccountId(request.getAccountId());
        app.setRequestedAmount(request.getAmount());
        app.setTermMonths(request.getTermMonths());
        app.setPurpose(request.getPurpose());
        app.setDecision(LoanApplication.LoanDecision.valueOf(
                node.get("decision").asText().toUpperCase().trim()));
        app.setRiskScore(node.get("riskScore").asInt());
        app.setInterestRate(new BigDecimal(node.get("interestRate").asText()));
        app.setApprovedAmount(new BigDecimal(node.get("approvedAmount").asText()));
        app.setReasoning(node.get("reasoning").asText());

        LoanApplication saved = repository.save(app);
        log.info(" Loan decision: {} (score={})",
                saved.getDecision(), saved.getRiskScore());
        return toResponse(saved);
    }
    public List<LoanApplicationResponse> listAll() {
        return repository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<LoanApplicationResponse> listByAccount(UUID accountId) {
        return repository.findByAccountIdOrderByCreatedAtDesc(accountId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private void requireField(JsonNode node, String field) {
        if (!node.has(field) || node.get(field).isNull()) {
            log.error(" AI response missing '{}' field. Full JSON:\n{}",
                    field, node.toPrettyString());
            throw new RuntimeException("AI response missing required field: " + field);
        }
    }
    private String extractJson(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new RuntimeException("Cannot extract JSON from empty response");
        }

        String s = raw.trim();

        // Strip markdown code fences
        s = s.replaceAll("(?i)```json", "")
                .replaceAll("(?i)```", "")
                .trim();

        // Find first '{' and matching last '}'
        int first = s.indexOf('{');
        int last = s.lastIndexOf('}');

        if (first < 0 || last < first) {
            throw new RuntimeException("No JSON object found in: " + raw);
        }

        s = s.substring(first, last + 1);

        // Remove trailing commas before } or ]
        s = s.replaceAll(",\\s*}", "}").replaceAll(",\\s*]", "]");

        return s;
    }

    private LoanApplicationResponse toResponse(LoanApplication a) {
        return new LoanApplicationResponse(
                a.getId(),
                a.getAccountId(),
                a.getRequestedAmount(),
                a.getTermMonths(),
                a.getPurpose(),
                a.getDecision().name(),
                a.getApprovedAmount(),
                a.getInterestRate(),
                a.getRiskScore(),
                a.getReasoning(),
                a.getCreatedAt()
        );
    }
}