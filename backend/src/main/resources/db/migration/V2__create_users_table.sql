CREATE TABLE users (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email                   VARCHAR(255) NOT NULL,
    password_hash           VARCHAR(255) NOT NULL,
    display_name            VARCHAR(100) NOT NULL,
    is_locked               BOOLEAN NOT NULL DEFAULT FALSE,
    failed_login_attempts   INTEGER NOT NULL DEFAULT 0,
    locked_until            TIMESTAMP WITH TIME ZONE,
    password_reset_token    VARCHAR(255),
    password_reset_expiry   TIMESTAMP WITH TIME ZONE,
    created_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version                 INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT ck_users_email_length CHECK (char_length(email) >= 3),
    CONSTRAINT ck_users_display_name_length CHECK (char_length(display_name) >= 1),
    CONSTRAINT ck_users_failed_attempts CHECK (failed_login_attempts >= 0)
);

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_password_reset_token ON users (password_reset_token)
    WHERE password_reset_token IS NOT NULL;
