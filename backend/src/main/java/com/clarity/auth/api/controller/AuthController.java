package com.clarity.auth.api.controller;

import com.clarity.auth.api.dto.*;
import com.clarity.auth.application.service.AuthApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication controller placeholder.
 * Full implementation in Sprint 2.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthApplicationService authApplicationService;

    public AuthController(AuthApplicationService authApplicationService) {
        this.authApplicationService = authApplicationService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        // TODO: Implement in Sprint 2
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        // TODO: Implement in Sprint 2
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        // TODO: Implement in Sprint 2
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        // TODO: Implement in Sprint 2
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        // TODO: Implement in Sprint 2
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        // TODO: Implement in Sprint 2
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
