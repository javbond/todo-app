package com.clarity.task.application.command;

import com.clarity.task.domain.model.ListColor;

import java.util.UUID;

public record CreateListCommand(
        String name,
        ListColor color,
        UUID userId
) {}
