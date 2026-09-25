package com.smartbank.account.bill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
@Order(1)
public class BillerSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(BillerSeeder.class);

    private final BillerRepository repository;

    public BillerSeeder(BillerRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (repository.count() > 0) {
            log.info("Billers already seeded ({} found)", repository.count());
            return;
        }

        List<Biller> defaults = List.of(
                biller("ELEC-BESCOM",   "BESCOM Electricity",     "ELECTRICITY", "1500"),
                biller("WATER-BWSSB",   "BWSSB Water Board",      "WATER",       "500"),
                biller("PHONE-AIRTEL",  "Airtel Mobile",          "PHONE",       "800"),
                biller("NET-JIO",       "Jio Fiber Broadband",    "INTERNET",    "999"),
                biller("DTH-TATA",      "Tata Play DTH",          "DTH",         "450"),
                biller("GAS-GAIL",      "GAIL Gas Connection",    "GAS",         "700"),
                biller("RENT-LANDLORD", "Landlord Rent",          "RENT",        "15000"),
                biller("INS-LIC",       "LIC Insurance Premium",  "INSURANCE",   "2000"),
                biller("EDU-SCHOOL",    "School Fees",            "EDUCATION",   "5000"),
                biller("OTHR-MISC",     "Other Bill",             "OTHER",       "1000")
        );

        repository.saveAll(defaults);
        log.info("=== Seeded {} default billers ===", defaults.size());
    }

    private Biller biller(String code, String name, String category, String defaultAmount) {
        Biller b = new Biller();
        b.setCode(code);
        b.setName(name);
        b.setCategory(category);
        b.setDefaultAmount(new BigDecimal(defaultAmount));
        return b;
    }
}