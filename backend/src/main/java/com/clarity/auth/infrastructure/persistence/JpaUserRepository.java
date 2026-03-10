package com.clarity.auth.infrastructure.persistence;

import com.clarity.auth.domain.model.User;
import com.clarity.auth.domain.repository.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter: JPA implementation of UserRepository (domain port).
 */
@Repository
public class JpaUserRepository implements UserRepository {

    private final SpringDataUserRepository springDataUserRepository;

    public JpaUserRepository(SpringDataUserRepository springDataUserRepository) {
        this.springDataUserRepository = springDataUserRepository;
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = UserJpaEntity.fromDomain(user);
        UserJpaEntity saved = springDataUserRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<User> findById(UUID id) {
        return springDataUserRepository.findById(id).map(UserJpaEntity::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return springDataUserRepository.findByEmail(email).map(UserJpaEntity::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return springDataUserRepository.existsByEmail(email);
    }

    @Override
    public void delete(User user) {
        springDataUserRepository.findById(user.getId()).ifPresent(springDataUserRepository::delete);
    }
}
