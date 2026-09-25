package com.smartbank.account.budget;
import com.smartbank.account.entity.Account;
import com.smartbank.account.entity.Transaction;
import com.smartbank.account.repository.AccountRepository;
import com.smartbank.account.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BudgetService {

    private static final Logger log = LoggerFactory.getLogger(BudgetService.class);

    private final BudgetRepository budgetRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public BudgetService(BudgetRepository budgetRepository,
                         AccountRepository accountRepository,
                         TransactionRepository transactionRepository) {
        this.budgetRepository = budgetRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    // =========================================================
    // Category keyword matching (mirrors frontend categorize.ts)
    // =========================================================

    private static final Map<String, String[]> CATEGORY_KEYWORDS = new LinkedHashMap<>() {{
        put("Rent",          new String[]{"rent", "landlord", "lease", "housing"});
        put("Groceries",     new String[]{"grocery", "market", "supermart", "supermarket", "vegetables", "kirana"});
        put("Utilities",     new String[]{"electricity", "water bill", "gas bill", "utility", "broadband", "internet bill", "mobile bill"});
        put("Dining",        new String[]{"restaurant", "cafe", "coffee", "swiggy", "zomato", "food", "pizza", "burger"});
        put("Shopping",      new String[]{"amazon", "flipkart", "shop", "store", "mall", "clothing", "myntra"});
        put("Transport",     new String[]{"uber", "ola", "cab", "taxi", "fuel", "petrol", "diesel", "metro", "bus"});
        put("Subscriptions", new String[]{"netflix", "spotify", "subscription", "prime", "hotstar", "youtube premium"});
        put("Transfer",      new String[]{"transfer"});
        put("Income",        new String[]{"salary", "income", "freelance", "bonus", "payroll", "paycheck"});
    }};

    public static String categorize(String description) {
        if (description == null || description.isBlank()) return "Other";
        String lower = description.toLowerCase();
        for (Map.Entry<String, String[]> e : CATEGORY_KEYWORDS.entrySet()) {
            for (String kw : e.getValue()) {
                if (lower.contains(kw)) return e.getKey();
            }
        }
        return "Other";
    }

    // =========================================================
    // CRUD
    // =========================================================

    @Transactional
    public BudgetResponse create(String userId, BudgetRequest req) {
        String category = req.getCategory().trim();
        if (budgetRepository.existsByUserIdAndCategory(userId, category)) {
            throw new RuntimeException("Budget for category '" + category + "' already exists");
        }

        Budget b = new Budget();
        b.setUserId(userId);
        b.setCategory(category);
        b.setMonthlyLimit(req.getMonthlyLimit());

        Budget saved = budgetRepository.save(b);
        return toResponse(saved, BigDecimal.ZERO);
    }

    @Transactional(readOnly = true)
    public List<BudgetResponse> listForUser(String userId) {
        List<Budget> budgets = budgetRepository.findByUserIdOrderByCategoryAsc(userId);
        if (budgets.isEmpty()) return List.of();

        Map<String, BigDecimal> spends = computeMonthlySpendByCategory();

        return budgets.stream()
                .map(b -> toResponse(b, spends.getOrDefault(b.getCategory(), BigDecimal.ZERO)))
                .collect(Collectors.toList());
    }

    @Transactional
    public BudgetResponse update(UUID id, String userId, BudgetRequest req) {
        Budget b = budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget not found: " + id));

        if (!b.getUserId().equals(userId)) {
            throw new RuntimeException("Not authorized to modify this budget");
        }

        b.setMonthlyLimit(req.getMonthlyLimit());
        Budget saved = budgetRepository.save(b);

        Map<String, BigDecimal> spends = computeMonthlySpendByCategory();
        return toResponse(saved, spends.getOrDefault(saved.getCategory(), BigDecimal.ZERO));
    }

    @Transactional
    public void delete(UUID id, String userId) {
        Budget b = budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget not found: " + id));

        if (!b.getUserId().equals(userId)) {
            throw new RuntimeException("Not authorized to delete this budget");
        }
        budgetRepository.delete(b);
    }

    // =========================================================
    // Monthly spend calculation
    // =========================================================

    /**
     * Compute this month's spend per category (across ALL accounts).
     * Returns a map: category → total spent (only withdrawals, excludes Income).
     */
    public Map<String, BigDecimal> computeMonthlySpendByCategory() {
        LocalDateTime startOfMonth = YearMonth.now().atDay(1).atStartOfDay();

        List<Account> accounts = accountRepository.findAll();
        Map<String, BigDecimal> spendByCategory = new HashMap<>();

        for (Account account : accounts) {
            List<Transaction> txs = transactionRepository
                    .findByAccountIdOrderByCreatedAtDesc(account.getId());

            for (Transaction t : txs) {
                if (!"WITHDRAWAL".equals(t.getType().name())) continue;
                if (t.getCreatedAt().isBefore(startOfMonth)) continue;

                String cat = categorize(t.getDescription());
                if ("Income".equals(cat)) continue;

                spendByCategory.merge(cat, t.getAmount(), BigDecimal::add);
            }
        }

        log.debug("Monthly spend by category: {}", spendByCategory);
        return spendByCategory;
    }

    // =========================================================
    // Helpers
    // =========================================================

    private BudgetResponse toResponse(Budget b, BigDecimal currentSpend) {
        BigDecimal limit = b.getMonthlyLimit();
        double pct = limit.signum() > 0
                ? currentSpend.divide(limit, 4, RoundingMode.HALF_UP).doubleValue() * 100
                : 0;

        String status;
        if (pct >= 100) status = "EXCEEDED";
        else if (pct >= 80) status = "WARNING";
        else status = "OK";

        return new BudgetResponse(
                b.getId(),
                b.getCategory(),
                limit,
                currentSpend,
                Math.round(pct * 10.0) / 10.0,
                status,
                b.getCreatedAt()
        );
    }
}