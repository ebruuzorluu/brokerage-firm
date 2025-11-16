package org.example.brokerage.dto;

import lombok.*;

@Data
@Builder
public class LoginResponse {
    private String token;
    private String username;
    private String role;
    private Long customerId;
}
