package com.cropdeal.authservice.dto;

import com.cropdeal.authservice.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String tokenType;
    private Long userId;
    private String email;
    private Role role;
    private String fullName;
    private long expiresIn;
}
