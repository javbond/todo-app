package com.clarity.auth.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * RefreshToken entity — separate from the User aggregate.
 */
public class RefreshToken {

    private UUID id;
    private String token;
    private UUID userId;
    private OffsetDateTime expiresAt;
    private boolean revoked;
    private OffsetDateTime createdAt;

    RefreshToken() {}

    private RefreshToken(Builder builder) {
        this.id = builder.id;
        this.token = builder.token;
        this.userId = builder.userId;
        this.expiresAt = builder.expiresAt;
        this.revoked = builder.revoked;
        this.createdAt = builder.createdAt;
    }

    public static RefreshToken create(UUID userId, int validDays) {
        return new Builder()
                .id(UUID.randomUUID())
                .token(UUID.randomUUID().toString())
                .userId(userId)
                .expiresAt(OffsetDateTime.now().plusDays(validDays))
                .revoked(false)
                .createdAt(OffsetDateTime.now())
                .build();
    }

    public void revoke() {
        this.revoked = true;
    }

    public boolean isExpired() {
        return OffsetDateTime.now().isAfter(this.expiresAt);
    }

    public UUID getId() { return id; }
    public String getToken() { return token; }
    public UUID getUserId() { return userId; }
    public OffsetDateTime getExpiresAt() { return expiresAt; }
    public boolean isRevoked() { return revoked; }
    public OffsetDateTime getCreatedAt() { return createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID id;
        private String token;
        private UUID userId;
        private OffsetDateTime expiresAt;
        private boolean revoked;
        private OffsetDateTime createdAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder token(String token) { this.token = token; return this; }
        public Builder userId(UUID userId) { this.userId = userId; return this; }
        public Builder expiresAt(OffsetDateTime expiresAt) { this.expiresAt = expiresAt; return this; }
        public Builder revoked(boolean revoked) { this.revoked = revoked; return this; }
        public Builder createdAt(OffsetDateTime createdAt) { this.createdAt = createdAt; return this; }

        public RefreshToken build() { return new RefreshToken(this); }
    }
}
