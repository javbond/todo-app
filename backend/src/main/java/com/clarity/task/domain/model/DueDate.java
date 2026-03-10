package com.clarity.task.domain.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Value object wrapping a LocalDate for task due dates.
 */
public final class DueDate {

    private final LocalDate date;

    public DueDate(LocalDate date) {
        this.date = Objects.requireNonNull(date, "Due date cannot be null");
    }

    public static DueDate of(LocalDate date) {
        return new DueDate(date);
    }

    public LocalDate getValue() {
        return date;
    }

    public boolean isOverdue() {
        return date.isBefore(LocalDate.now());
    }

    public boolean isToday() {
        return date.isEqual(LocalDate.now());
    }

    public boolean isDueWithin(int days) {
        LocalDate cutoff = LocalDate.now().plusDays(days);
        return !date.isAfter(cutoff) && !date.isBefore(LocalDate.now());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DueDate other)) return false;
        return Objects.equals(date, other.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date);
    }

    @Override
    public String toString() {
        return date.toString();
    }
}
