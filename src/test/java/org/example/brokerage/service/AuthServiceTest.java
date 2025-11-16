package org.example.brokerage.service;

import org.example.brokerage.dto.LoginRequest;
import org.example.brokerage.dto.LoginResponse;
import org.example.brokerage.model.Customer;
import org.example.brokerage.repository.CustomerRepository;
import org.example.brokerage.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private Customer customer;
    private Customer adminCustomer;

    @BeforeEach
    void setUp() {
        customer = Customer.builder()
                .id(1L)
                .username("customer1")
                .password("encodedPassword123")
                .role("CUSTOMER")
                .build();

        adminCustomer = Customer.builder()
                .id(999L)
                .username("admin")
                .password("encodedAdminPass")
                .role("ADMIN")
                .build();
    }

    @Test
    void login_Customer_Success() {
        LoginRequest request = new LoginRequest();
        request.setUsername("customer1");
        request.setPassword("password123");

        when(customerRepository.findByUsername("customer1"))
                .thenReturn(Optional.of(customer));
        when(passwordEncoder.matches("password123", "encodedPassword123"))
                .thenReturn(true);
        when(jwtUtil.generateToken("customer1", 1L, "CUSTOMER"))
                .thenReturn("jwt-token-123");

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token-123", response.getToken());
        assertEquals(1L, response.getCustomerId());
        assertEquals("customer1", response.getUsername());
        assertEquals("CUSTOMER", response.getRole());
        verify(customerRepository).findByUsername("customer1");
        verify(passwordEncoder).matches("password123", "encodedPassword123");
        verify(jwtUtil).generateToken("customer1", 1L, "CUSTOMER");
    }

    @Test
    void login_Admin_Success() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("adminPass123");

        when(customerRepository.findByUsername("admin"))
                .thenReturn(Optional.of(adminCustomer));
        when(passwordEncoder.matches("adminPass123", "encodedAdminPass"))
                .thenReturn(true);
        when(jwtUtil.generateToken("admin", 999L, "ADMIN"))
                .thenReturn("admin-jwt-token");

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("admin-jwt-token", response.getToken());
        assertEquals(999L, response.getCustomerId());
        assertEquals("admin", response.getUsername());
        assertEquals("ADMIN", response.getRole());
    }

    @Test
    void login_UserNotFound() {
        LoginRequest request = new LoginRequest();
        request.setUsername("nonexistent");
        request.setPassword("password123");

        when(customerRepository.findByUsername("nonexistent"))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            authService.login(request);
        });
        verify(customerRepository).findByUsername("nonexistent");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyString(), anyLong(), anyString());
    }

    @Test
    void login_InvalidPassword() {
        LoginRequest request = new LoginRequest();
        request.setUsername("customer1");
        request.setPassword("wrongPassword");

        when(customerRepository.findByUsername("customer1"))
                .thenReturn(Optional.of(customer));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword123"))
                .thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> {
            authService.login(request);
        });
        verify(customerRepository).findByUsername("customer1");
        verify(passwordEncoder).matches("wrongPassword", "encodedPassword123");
        verify(jwtUtil, never()).generateToken(anyString(), anyLong(), anyString());
    }

    @Test
    void login_EmptyUsername() {
        LoginRequest request = new LoginRequest();
        request.setUsername("");
        request.setPassword("password123");

        when(customerRepository.findByUsername(""))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            authService.login(request);
        });
    }

    @Test
    void login_NullPassword() {
        LoginRequest request = new LoginRequest();
        request.setUsername("customer1");
        request.setPassword(null);

        when(customerRepository.findByUsername("customer1"))
                .thenReturn(Optional.of(customer));
        when(passwordEncoder.matches(null, "encodedPassword123"))
                .thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> {
            authService.login(request);
        });
    }
}
