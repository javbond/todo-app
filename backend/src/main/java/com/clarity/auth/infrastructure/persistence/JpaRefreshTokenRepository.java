package com.clarity.auth.infrastructure.persistence;

import com.clarity.auth.domain.model.RefreshToken;
import com.clarity.auth.domain.repository.RefreshTokenRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter: JPA implementation of RefreshTokenRepository (domain port).
 */
@Repository
public class JpaRefreshTokenRepository implements RefreshTokenRepository {

    private final SpringDataRefreshTokenRepository springDataRefreshTokenRepository;

    public JpaRefreshTokenRepository(SpringDataRefreshTokenRepository springDataRefreshTokenRepository) {
        this.springDataRefreshTokenRepository = springDataRefreshTokenRepository;
    }

    @Override
    public RefreshToken save(RefreshToken token) {
        RefreshTokenJpaEntity entity = RefreshTokenJpaEntity.fromDomain(token);
        RefreshTokenJpaEntity saved = springDataRefreshTokenRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return springDataRefreshTokenRepository.findByToken(token).map(RefreshTokenJpaEntity::toDomain);
    }

    @Override
    public List<RefreshToken> findAllByUserId(UUID userId) {
        return springDataRefreshTokenRepository.findAllByUserId(userId).stream()
                .map(RefreshTokenJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByUserId(UUID userId) {
        springDataRefreshTokenRepository.deleteByUserId(userId);
    }

    @Override
    public void delete(RefreshToken token) {
        springDataRefreshTokenRepository.findById(token.getId())
                .ifPresent(springDataRefreshTokenRepository::delete);
    }
}
