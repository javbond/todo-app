package com.clarity.task.application.command;

import com.clarity.task.domain.model.ListColor;

import java.util.UUID;

public record UpdateListCommand(
        UUID listId,
        UUID userId,
        String name,
        ListColor color
) {}
