package com.clarity.auth.domain.repository;

import com.clarity.auth.domain.model.User;

import java.util.Optional;
import java.util.UUID;

/**
 * Port: User repository interface (domain layer).
 */
public interface UserRepository {

    User save(User user);

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    void delete(User user);
}
