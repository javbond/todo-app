package com.clarity.auth.application.service;

import com.clarity.auth.domain.repository.RefreshTokenRepository;
import com.clarity.auth.domain.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for authentication use cases.
 * Placeholder implementation — full auth in Sprint 2.
 */
@Service
@Transactional
public class AuthApplicationService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthApplicationService(UserRepository userRepository,
                                    RefreshTokenRepository refreshTokenRepository,
                                    PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Object register(String email, String password, String displayName) {
        // TODO: Implement in Sprint 2 — create user, create default inbox, publish UserRegisteredEvent
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public Object login(String email, String password) {
        // TODO: Implement in Sprint 2 — validate credentials, generate JWT + refresh token
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public Object refreshToken(String refreshToken) {
        // TODO: Implement in Sprint 2 — rotate refresh token, issue new JWT
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void logout(String userId, String jti) {
        // TODO: Implement in Sprint 2 — blacklist JWT, revoke refresh tokens
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
