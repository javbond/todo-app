package com.clarity.auth.infrastructure.persistence;

import com.clarity.auth.domain.model.Email;
import com.clarity.auth.domain.model.User;
import com.clarity.shared.domain.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class UserJpaEntity extends AuditableEntity {

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "is_locked", nullable = false)
    private boolean locked;

    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts;

    @Column(name = "locked_until")
    private OffsetDateTime lockedUntil;

    @Column(name = "password_reset_token", length = 255)
    private String passwordResetToken;

    @Column(name = "password_reset_expiry")
    private OffsetDateTime passwordResetExpiry;

    // ─── Conversion ───────────────────────────────────────────────────────────

    public User toDomain() {
        return User.builder()
                .id(this.getId())
                .email(new Email(this.email))
                .passwordHash(this.passwordHash)
                .displayName(this.displayName)
                .locked(this.locked)
                .failedLoginAttempts(this.failedLoginAttempts)
                .lockedUntil(this.lockedUntil)
                .passwordResetToken(this.passwordResetToken)
                .passwordResetExpiry(this.passwordResetExpiry)
                .createdAt(this.getCreatedAt())
                .updatedAt(this.getUpdatedAt())
                .version(this.getVersion() != null ? this.getVersion() : 0)
                .build();
    }

    public static UserJpaEntity fromDomain(User user) {
        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(user.getId());
        entity.setEmail(user.getEmail().getValue());
        entity.setPasswordHash(user.getPasswordHash());
        entity.setDisplayName(user.getDisplayName());
        entity.setLocked(user.isLocked());
        entity.setFailedLoginAttempts(user.getFailedLoginAttempts());
        entity.setLockedUntil(user.getLockedUntil());
        entity.setPasswordResetToken(user.getPasswordResetToken());
        entity.setPasswordResetExpiry(user.getPasswordResetExpiry());
        entity.setVersion(user.getVersion());
        return entity;
    }
}
