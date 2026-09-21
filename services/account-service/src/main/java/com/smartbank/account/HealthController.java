package com.smartbank.account;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
@RestController
public class HealthController {
    @GetMapping("/hello")
    public Map<String, String> hello() {
        return Map.of(
                "service", "account-service",
                "status", "UP",
                "message", "Welcome to Smart Banking!"
        );
    }
}