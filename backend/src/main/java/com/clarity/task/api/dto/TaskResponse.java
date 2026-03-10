package com.clarity.task.api.dto;

import com.clarity.task.domain.model.Priority;
import com.clarity.task.domain.model.ReminderInterval;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        String title,
        String description,
        Priority priority,
        LocalDate dueDate,
        ReminderInterval reminderInterval,
        boolean completed,
        boolean deleted,
        boolean today,
        OffsetDateTime completedAt,
        int sortOrder,
        UUID listId,
        UUID userId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        int version
) {}
