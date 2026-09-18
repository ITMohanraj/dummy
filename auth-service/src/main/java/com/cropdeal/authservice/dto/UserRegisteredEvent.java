package com.cropdeal.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisteredEvent implements Serializable {
    private Long userId;
    private String email;
    private String fullName;
    private String role;
    private String phone;
    private LocalDateTime registeredAt;
}
