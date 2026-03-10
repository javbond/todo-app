package com.clarity.task.api.dto;

import com.clarity.task.domain.model.Priority;
import com.clarity.task.domain.model.ReminderInterval;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateTaskRequest(
        @NotBlank(message = "Title is required")
        @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
        String title,

        @Size(max = 2000, message = "Description cannot exceed 2000 characters")
        String description,

        Priority priority,

        LocalDate dueDate,

        ReminderInterval reminderInterval
) {}
