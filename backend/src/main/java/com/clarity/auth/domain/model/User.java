package com.clarity.auth.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * User entity — aggregate root for the Identity & Access bounded context.
 */
public class User {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_MINUTES = 15;

    private UUID id;
    private Email email;
    private String passwordHash;
    private String displayName;
    private boolean locked;
    private int failedLoginAttempts;
    private OffsetDateTime lockedUntil;
    private String passwordResetToken;
    private OffsetDateTime passwordResetExpiry;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private int version;

    User() {}

    private User(Builder builder) {
        this.id = builder.id;
        this.email = builder.email;
        this.passwordHash = builder.passwordHash;
        this.displayName = builder.displayName;
        this.locked = builder.locked;
        this.failedLoginAttempts = builder.failedLoginAttempts;
        this.lockedUntil = builder.lockedUntil;
        this.passwordResetToken = builder.passwordResetToken;
        this.passwordResetExpiry = builder.passwordResetExpiry;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
        this.version = builder.version;
    }

    // ─── Factory ──────────────────────────────────────────────────────────────

    public static User create(String emailValue, String passwordHash, String displayName) {
        OffsetDateTime now = OffsetDateTime.now();
        return new Builder()
                .id(UUID.randomUUID())
                .email(new Email(emailValue))
                .passwordHash(passwordHash)
                .displayName(displayName)
                .locked(false)
                .failedLoginAttempts(0)
                .createdAt(now)
                .updatedAt(now)
                .version(0)
                .build();
    }

    // ─── Business Methods ─────────────────────────────────────────────────────

    public void recordFailedLogin() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= MAX_FAILED_ATTEMPTS) {
            lock(OffsetDateTime.now().plusMinutes(LOCKOUT_MINUTES));
        }
        this.updatedAt = OffsetDateTime.now();
    }

    public void resetFailedAttempts() {
        this.failedLoginAttempts = 0;
        this.updatedAt = OffsetDateTime.now();
    }

    public void lock(OffsetDateTime until) {
        this.locked = true;
        this.lockedUntil = until;
        this.updatedAt = OffsetDateTime.now();
    }

    public void unlock() {
        this.locked = false;
        this.lockedUntil = null;
        this.failedLoginAttempts = 0;
        this.updatedAt = OffsetDateTime.now();
    }

    public boolean isAccountLocked() {
        if (!this.locked) return false;
        if (this.lockedUntil != null && OffsetDateTime.now().isAfter(this.lockedUntil)) {
            return false; // Lock has expired
        }
        return true;
    }

    // ─── Getters ──────────────────────────────────────────────────────────────

    public UUID getId() { return id; }
    public Email getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getDisplayName() { return displayName; }
    public boolean isLocked() { return locked; }
    public int getFailedLoginAttempts() { return failedLoginAttempts; }
    public OffsetDateTime getLockedUntil() { return lockedUntil; }
    public String getPasswordResetToken() { return passwordResetToken; }
    public OffsetDateTime getPasswordResetExpiry() { return passwordResetExpiry; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public int getVersion() { return version; }

    // ─── Builder ──────────────────────────────────────────────────────────────

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private Email email;
        private String passwordHash;
        private String displayName;
        private boolean locked;
        private int failedLoginAttempts;
        private OffsetDateTime lockedUntil;
        private String passwordResetToken;
        private OffsetDateTime passwordResetExpiry;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;
        private int version;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder email(Email email) { this.email = email; return this; }
        public Builder passwordHash(String passwordHash) { this.passwordHash = passwordHash; return this; }
        public Builder displayName(String displayName) { this.displayName = displayName; return this; }
        public Builder locked(boolean locked) { this.locked = locked; return this; }
        public Builder failedLoginAttempts(int failedLoginAttempts) { this.failedLoginAttempts = failedLoginAttempts; return this; }
        public Builder lockedUntil(OffsetDateTime lockedUntil) { this.lockedUntil = lockedUntil; return this; }
        public Builder passwordResetToken(String passwordResetToken) { this.passwordResetToken = passwordResetToken; return this; }
        public Builder passwordResetExpiry(OffsetDateTime passwordResetExpiry) { this.passwordResetExpiry = passwordResetExpiry; return this; }
        public Builder createdAt(OffsetDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public Builder version(int version) { this.version = version; return this; }

        public User build() { return new User(this); }
    }
}
