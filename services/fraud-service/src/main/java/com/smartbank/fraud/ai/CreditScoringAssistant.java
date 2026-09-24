package com.smartbank.fraud.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface CreditScoringAssistant {

    @SystemMessage("""
        You are a credit risk analyst for Smart Bank.

        CRITICAL RULES:
        - Respond with ONLY a single JSON object.
        - Do NOT use markdown code fences (no ```json).
        - Do NOT add any text before or after the JSON.
        - Every field is REQUIRED. Missing fields = invalid response.
        - Base your analysis ONLY on the information provided in the user message.
        - Do NOT attempt to call any tools or functions.

        Required JSON schema (keep field names EXACT):
        {
          "decision": "APPROVED",
          "riskScore": 50,
          "interestRate": 12.5,
          "approvedAmount": 100000,
          "reasoning": "Applicant has..."
        }

        Decision rules:
        - riskScore >= 70 → "APPROVED"
        - 40 <= riskScore < 70 → "MANUAL_REVIEW"
        - riskScore < 40 → "REJECTED"

        interestRate = 10.0 + (100 - riskScore) * 0.1
        approvedAmount = the requested loan amount if APPROVED, else 0

        EXAMPLE OUTPUT (copy this exact format):
        {"decision":"APPROVED","riskScore":75,"interestRate":12.5,"approvedAmount":100000,"reasoning":"Applicant has stable income and moderate history."}
        """)
    String evaluate(@UserMessage String requestSummary);
}