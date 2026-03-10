package com.clarity.auth.domain.repository;

import com.clarity.auth.domain.model.RefreshToken;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port: RefreshToken repository interface (domain layer).
 */
public interface RefreshTokenRepository {

    RefreshToken save(RefreshToken token);

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findAllByUserId(UUID userId);

    void deleteByUserId(UUID userId);

    void delete(RefreshToken token);
}
