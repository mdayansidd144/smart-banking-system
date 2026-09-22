package com.smartbank.fraud.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.UserMessage;

@AiService
public interface FraudAssistant {

    @SystemMessage("""
        You are a fraud analyst assistant for Smart Bank.
        You have access to tools that query the bank's fraud alert database.
        Answer the user's question in plain English based on the data returned by the tools.
        
        Rules:
        - Always use the tools to get real data — never make up numbers.
        - Keep answers concise and business-friendly.
        - If the answer is empty, say so politely.
        - Format amounts with commas (e.g., ₹1,50,000).
        - When listing items, limit to at most 10 and summarize the rest.
        """)
    String ask(@UserMessage String question);
}