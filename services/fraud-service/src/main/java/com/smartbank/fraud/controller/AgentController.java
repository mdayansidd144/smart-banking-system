package com.smartbank.fraud.controller;
import com.smartbank.fraud.ai.BankingAgentAssistant;
import com.smartbank.fraud.dto.AgentChatRequest;
import com.smartbank.fraud.dto.AgentChatResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
@RestController
@RequestMapping("/api/v1/agent")
public class AgentController {

    private static final Logger log = LoggerFactory.getLogger(AgentController.class);

    private final BankingAgentAssistant assistant;

    public AgentController(BankingAgentAssistant assistant) {
        this.assistant = assistant;
    }

    @PostMapping("/chat")
    public ResponseEntity<AgentChatResponse> chat(
            @Valid @RequestBody AgentChatRequest request) {

        // Use provided sessionId, or generate one for a new conversation
        String sessionId = (request.getSessionId() != null && !request.getSessionId().isBlank())
                ? request.getSessionId()
                : UUID.randomUUID().toString();

        log.info("[session={}] Agent chat: {}", sessionId, request.getMessage());

        try {
            String reply = assistant.chat(sessionId, request.getMessage());
            log.info("[session={}] Agent reply: {}", sessionId, reply);
            return ResponseEntity.ok(new AgentChatResponse(reply, sessionId));
        } catch (Exception e) {
            log.error("[session={}] Agent error: {}", sessionId, e.getMessage(), e);
            return ResponseEntity.ok(new AgentChatResponse(
                    "Sorry, I encountered an error while processing your request.",
                    sessionId));
        }
    }
}