package com.enterprise.hrms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Data Transfer Object containing the refresh token to request a new access token.
 */
@Data
public class TokenRefreshRequest {
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
