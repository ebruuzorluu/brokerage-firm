package org.example.brokerage.service;

import org.example.brokerage.dto.LoginRequest;
import org.example.brokerage.dto.LoginResponse;
import org.example.brokerage.model.Customer;
import org.example.brokerage.repository.CustomerRepository;
import org.example.brokerage.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        Customer customer = customerRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), customer.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(
                customer.getUsername(),
                customer.getId(),
                customer.getRole()
        );

        return LoginResponse.builder()
                .token(token)
                .username(customer.getUsername())
                .role(customer.getRole())
                .customerId(customer.getId())
                .build();
    }
}
