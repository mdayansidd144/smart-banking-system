package com.smartbank.account.fx;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class FxRateService {

    private static final Logger log = LoggerFactory.getLogger(FxRateService.class);

    /** Currencies we support (as targets against USD base). */
    public static final Set<String> SUPPORTED_CURRENCIES = Set.of(
            "USD", "INR", "EUR", "GBP", "JPY", "AUD", "CAD", "SGD", "AED", "CHF"
    );

    /** Seed rates (fallback if API fails). Rates are vs 1 USD. */
    private static final Map<String, BigDecimal> SEED_RATES = Map.of(
            "USD", new BigDecimal("1.0"),
            "INR", new BigDecimal("83.50"),
            "EUR", new BigDecimal("0.92"),
            "GBP", new BigDecimal("0.79"),
            "JPY", new BigDecimal("149.50"),
            "AUD", new BigDecimal("1.52"),
            "CAD", new BigDecimal("1.36"),
            "SGD", new BigDecimal("1.34"),
            "AED", new BigDecimal("3.67"),
            "CHF", new BigDecimal("0.88")
    );

    private final FxRateRepository repository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${fx.api.url:https://api.exchangerate.host/live}")
    private String apiUrl;

    @Value("${fx.api.key:}")
    private String apiKey;

    public FxRateService(FxRateRepository repository) {
        this.repository = repository;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @PostConstruct
    public void init() {
        // Ensure seed rates exist immediately (so app works before first API call)
        try {
            seedIfEmpty();
            log.info("=== FxRateService initialized with {} rates ===", repository.count());
        } catch (Exception e) {
            log.warn("Failed to seed FxRates: {}", e.getMessage());
        }
    }

    // =========================================================
    // Refresh from API
    // =========================================================

    /** Refresh every 6 hours. Runs 30 seconds after startup. */
    @Scheduled(fixedDelay = 6 * 60 * 60 * 1000, initialDelay = 30 * 1000)
    @Transactional
    public void refreshRates() {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("FX API key not configured — using seed rates only");
            return;
        }
        try {
            String url = apiUrl + "?access_key=" + apiKey + "&source=USD";
            log.info("Fetching FX rates from {}", apiUrl);
            String response = restTemplate.getForObject(url, String.class);

            JsonNode root = objectMapper.readTree(response);
            JsonNode quotes = root.get("quotes");
            if (quotes == null) {
                log.warn("No 'quotes' in API response: {}", response);
                return;
            }

            int updated = 0;
            for (String currency : SUPPORTED_CURRENCIES) {
                if ("USD".equals(currency)) continue;

                String quoteKey = "USD" + currency;
                JsonNode node = quotes.get(quoteKey);
                if (node == null) continue;

                BigDecimal rate = new BigDecimal(node.asText());
                upsert(currency, rate, "exchangerate.host");
                updated++;
            }

            log.info("=== Refreshed {} FX rates from API ===", updated);
        } catch (Exception e) {
            log.error("Failed to refresh FX rates: {} — keeping cached values", e.getMessage());
        }
    }

    // =========================================================
    // Conversion
    // =========================================================

    /**
     * Convert `amount` from `fromCurrency` to `toCurrency`.
     * All rates are vs USD internally.
     */
    public BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency) {
        if (fromCurrency == null || toCurrency == null) {
            throw new RuntimeException("Currency required");
        }
        String from = fromCurrency.toUpperCase();
        String to = toCurrency.toUpperCase();

        if (from.equals(to)) {
            return amount;
        }

        BigDecimal rateFrom = getRateToUSD(from);   // 1 unit of `from` = ? USD
        BigDecimal rateTo = getRateToUSD(to);       // 1 unit of `to` = ? USD

        // amount * rateFrom  → USD value
        // USD value / rateTo → target currency
        return amount
                .multiply(rateFrom)
                .divide(rateTo, 2, RoundingMode.HALF_UP);
    }

    /**
     * Returns the "rate" for a currency vs USD.
     * i.e., 1 unit of `currency` = X USD.
     */
    public BigDecimal getRateToUSD(String currency) {
        String c = currency.toUpperCase();
        if ("USD".equals(c)) return BigDecimal.ONE;

        // DB stores: 1 USD = X currency
        // We want: 1 currency = 1/X USD
        Optional<FxRate> rateOpt = repository.findByBaseCurrencyAndTargetCurrency("USD", c);
        if (rateOpt.isPresent() && rateOpt.get().getRate().signum() > 0) {
            return BigDecimal.ONE.divide(rateOpt.get().getRate(), 8, RoundingMode.HALF_UP);
        }

        // Fallback: seed map
        BigDecimal seed = SEED_RATES.get(c);
        if (seed == null || seed.signum() == 0) {
            throw new RuntimeException("Unsupported currency: " + c);
        }
        return BigDecimal.ONE.divide(seed, 8, RoundingMode.HALF_UP);
    }

    public List<FxRate> getAllRates() {
        return repository.findAll();
    }

    public Set<String> getSupportedCurrencies() {
        return SUPPORTED_CURRENCIES;
    }

    // =========================================================
    // Helpers
    // =========================================================

    private void seedIfEmpty() {
        if (repository.count() > 0) return;
        for (Map.Entry<String, BigDecimal> e : SEED_RATES.entrySet()) {
            if ("USD".equals(e.getKey())) continue;
            upsert(e.getKey(), e.getValue(), "seed");
        }
        log.info("Seeded {} FX rates", SEED_RATES.size() - 1);
    }

    private void upsert(String targetCurrency, BigDecimal rate, String source) {
        FxRate existing = repository
                .findByBaseCurrencyAndTargetCurrency("USD", targetCurrency)
                .orElse(null);

        if (existing == null) {
            existing = new FxRate();
            existing.setBaseCurrency("USD");
            existing.setTargetCurrency(targetCurrency);
        }
        existing.setRate(rate);
        existing.setSource(source);
        existing.setFetchedAt(LocalDateTime.now());
        repository.save(existing);
    }
}