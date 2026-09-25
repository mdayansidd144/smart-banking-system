package com.smartbank.account.fx;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/fx")
public class FxController {

    private final FxRateService service;

    public FxController(FxRateService service) {
        this.service = service;
    }

    @GetMapping("/rates")
    public ResponseEntity<Map<String, Object>> listRates() {
        List<FxRate> rates = service.getAllRates();

        Map<String, Object> body = new HashMap<>();
        body.put("base", "USD");
        body.put("supported", service.getSupportedCurrencies());
        body.put("rates", rates);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/convert")
    public ResponseEntity<Map<String, Object>> convert(
            @RequestParam BigDecimal amount,
            @RequestParam String from,
            @RequestParam String to) {

        BigDecimal converted = service.convert(amount, from, to);

        Map<String, Object> body = new HashMap<>();
        body.put("from", from.toUpperCase());
        body.put("to", to.toUpperCase());
        body.put("amount", amount);
        body.put("converted", converted);
        body.put("rate", converted.divide(amount, 6, java.math.RoundingMode.HALF_UP));
        return ResponseEntity.ok(body);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleError(RuntimeException ex) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", ex.getMessage()));
    }
}