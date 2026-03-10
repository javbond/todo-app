package com.clarity.auth.infrastructure.persistence;

import com.clarity.auth.domain.model.RefreshToken;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class RefreshTokenJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "token", nullable = false, unique = true, length = 255)
    private String token;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "is_revoked", nullable = false)
    private boolean revoked;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    // ─── Conversion ───────────────────────────────────────────────────────────

    public RefreshToken toDomain() {
        return RefreshToken.builder()
                .id(this.id)
                .token(this.token)
                .userId(this.userId)
                .expiresAt(this.expiresAt)
                .revoked(this.revoked)
                .createdAt(this.createdAt)
                .build();
    }

    public static RefreshTokenJpaEntity fromDomain(RefreshToken token) {
        RefreshTokenJpaEntity entity = new RefreshTokenJpaEntity();
        entity.setId(token.getId());
        entity.setToken(token.getToken());
        entity.setUserId(token.getUserId());
        entity.setExpiresAt(token.getExpiresAt());
        entity.setRevoked(token.isRevoked());
        return entity;
    }
}
