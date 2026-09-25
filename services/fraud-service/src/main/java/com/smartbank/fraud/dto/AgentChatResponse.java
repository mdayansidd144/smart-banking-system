package com.smartbank.fraud.dto;
import java.time.LocalDateTime;
public class AgentChatResponse {

    private String reply;
    private String sessionId;
    private LocalDateTime repliedAt;

    public AgentChatResponse() {
        this.repliedAt = LocalDateTime.now();
    }

    public AgentChatResponse(String reply, String sessionId) {
        this.reply = reply;
        this.sessionId = sessionId;
        this.repliedAt = LocalDateTime.now();
    }

    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public LocalDateTime getRepliedAt() { return repliedAt; }
    public void setRepliedAt(LocalDateTime repliedAt) { this.repliedAt = repliedAt; }
}