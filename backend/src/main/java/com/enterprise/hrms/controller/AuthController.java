package com.enterprise.hrms.controller;

import com.enterprise.hrms.dto.*;
import com.enterprise.hrms.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller managing user authentication, login, refresh-tokens, and new registrations.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    // Login endpoint
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        JwtResponse jwtResponse = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(jwtResponse);
    }

    // Register employee & user endpoint (Restricted to ADMIN or HR roles only)
    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        authService.registerUser(registerRequest);
        return new ResponseEntity<>(new MessageResponse("Employee and user account registered successfully!"), HttpStatus.CREATED);
    }

    // Refresh Token endpoint
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshAccessToken(@Valid @RequestBody TokenRefreshRequest tokenRefreshRequest) {
        TokenRefreshResponse response = authService.refreshAccessToken(tokenRefreshRequest);
        return ResponseEntity.ok(response);
    }
}
