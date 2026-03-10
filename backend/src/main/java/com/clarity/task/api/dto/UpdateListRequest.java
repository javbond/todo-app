package com.clarity.task.api.dto;

import com.clarity.task.domain.model.ListColor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateListRequest(
        @NotBlank(message = "List name is required")
        @Size(min = 1, max = 50, message = "List name must be between 1 and 50 characters")
        String name,

        ListColor color
) {}
