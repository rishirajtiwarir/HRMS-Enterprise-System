package com.enterprise.hrms.service;

import com.enterprise.hrms.dto.JwtResponse;
import com.enterprise.hrms.dto.LoginRequest;
import com.enterprise.hrms.dto.RegisterRequest;
import com.enterprise.hrms.dto.TokenRefreshRequest;
import com.enterprise.hrms.dto.TokenRefreshResponse;

public interface AuthService {
    JwtResponse authenticateUser(LoginRequest loginRequest);
    void registerUser(RegisterRequest registerRequest);
    TokenRefreshResponse refreshAccessToken(TokenRefreshRequest tokenRefreshRequest);
}
