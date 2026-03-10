package com.clarity.auth.domain.model;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value object representing a validated email address.
 */
public final class Email {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final String value;

    public Email(String value) {
        Objects.requireNonNull(value, "Email cannot be null");
        String trimmed = value.trim().toLowerCase();
        if (trimmed.length() < 3) {
            throw new IllegalArgumentException("Email must be at least 3 characters");
        }
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + value);
        }
        this.value = trimmed;
    }

    public static Email of(String value) {
        return new Email(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Email other)) return false;
        return Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
