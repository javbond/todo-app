package com.clarity.auth.infrastructure.security;

import org.springframework.stereotype.Component;

/**
 * JWT token provider placeholder.
 * Full implementation in Sprint 2.
 */
@Component
public class JwtTokenProvider {

    public String generateAccessToken(String userId, String email) {
        // TODO: Implement JWT generation in Sprint 2
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public String validateToken(String token) {
        // TODO: Implement JWT validation in Sprint 2
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public String extractUserId(String token) {
        // TODO: Implement JWT claim extraction in Sprint 2
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
