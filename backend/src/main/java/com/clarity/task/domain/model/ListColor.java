package com.clarity.task.domain.model;

public enum ListColor {

    RED("#EF4444"),
    ORANGE("#F97316"),
    YELLOW("#EAB308"),
    GREEN("#22C55E"),
    BLUE("#3B82F6"),
    PURPLE("#8B5CF6"),
    PINK("#EC4899"),
    GRAY("#9CA3AF");

    private final String hex;

    ListColor(String hex) {
        this.hex = hex;
    }

    public String getHex() {
        return hex;
    }
}
