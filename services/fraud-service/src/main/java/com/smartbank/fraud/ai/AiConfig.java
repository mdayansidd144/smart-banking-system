package com.smartbank.fraud.ai;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Duration;

@Configuration
public class AiConfig {

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return OllamaChatModel.builder()
                .baseUrl("http://host.docker.internal:11434")
                .modelName("llama3.2")
                .temperature(0.2)
                .timeout(Duration.ofSeconds(180))
                .logRequests(true)
                .logResponses(true)
                .build();
    }
}