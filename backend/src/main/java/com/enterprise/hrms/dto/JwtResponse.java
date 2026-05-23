package com.enterprise.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object for authentication success responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private String refreshToken;
    private Long id;
    private String username;
    private String email;
    private List<String> roles;
    private Long employeeId;

    public JwtResponse(String accessToken, String refreshToken, Long id, String username, String email, List<String> roles, Long employeeId) {
        this.token = accessToken;
        this.refreshToken = refreshToken;
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
        this.employeeId = employeeId;
    }
}
