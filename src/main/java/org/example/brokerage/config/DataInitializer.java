package org.example.brokerage.config;

import org.example.brokerage.model.Asset;
import org.example.brokerage.model.Customer;
import org.example.brokerage.repository.AssetRepository;
import org.example.brokerage.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final CustomerRepository customerRepository;
    private final AssetRepository assetRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Create admin user
        Customer admin = Customer.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .role("ADMIN")
                .build();
        customerRepository.save(admin);

        // Create test customers
        Customer customer1 = Customer.builder()
                .username("customer1")
                .password(passwordEncoder.encode("password123"))
                .role("CUSTOMER")
                .build();
        customer1 = customerRepository.save(customer1);

        Customer customer2 = Customer.builder()
                .username("customer2")
                .password(passwordEncoder.encode("password123"))
                .role("CUSTOMER")
                .build();
        customer2 = customerRepository.save(customer2);

        // Initialize TRY assets for customers
        Asset tryAsset1 = Asset.builder()
                .customerId(customer1.getId())
                .assetName("TRY")
                .size(new BigDecimal("100000"))
                .usableSize(new BigDecimal("100000"))
                .build();
        assetRepository.save(tryAsset1);

        Asset tryAsset2 = Asset.builder()
                .customerId(customer2.getId())
                .assetName("TRY")
                .size(new BigDecimal("50000"))
                .usableSize(new BigDecimal("50000"))
                .build();
        assetRepository.save(tryAsset2);

        // Add some stock assets for customer1
        Asset stockAsset = Asset.builder()
                .customerId(customer1.getId())
                .assetName("AAPL")
                .size(new BigDecimal("10"))
                .usableSize(new BigDecimal("10"))
                .build();
        assetRepository.save(stockAsset);
    }
}
