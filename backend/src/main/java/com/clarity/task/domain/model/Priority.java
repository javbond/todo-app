package com.clarity.task.domain.model;

public enum Priority {

    P1("Urgent", "#EF4444"),
    P2("High", "#F97316"),
    P3("Medium", "#3B82F6"),
    P4("Low", "#9CA3AF");

    private final String label;
    private final String color;

    Priority(String label, String color) {
        this.label = label;
        this.color = color;
    }

    public String getLabel() {
        return label;
    }

    public String getColor() {
        return color;
    }
}
