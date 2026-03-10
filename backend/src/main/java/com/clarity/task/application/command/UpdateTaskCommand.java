package com.clarity.task.application.command;

import com.clarity.task.domain.model.Priority;
import com.clarity.task.domain.model.ReminderInterval;

import java.time.LocalDate;
import java.util.UUID;

public record UpdateTaskCommand(
        UUID taskId,
        UUID userId,
        String title,
        String description,
        Priority priority,
        LocalDate dueDate,
        ReminderInterval reminderInterval
) {}
