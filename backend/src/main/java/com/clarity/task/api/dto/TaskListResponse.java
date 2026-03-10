package com.clarity.task.api.dto;

import com.clarity.task.domain.model.ListColor;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TaskListResponse(
        UUID id,
        String name,
        ListColor color,
        int sortOrder,
        boolean defaultList,
        UUID userId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        int version
) {}
