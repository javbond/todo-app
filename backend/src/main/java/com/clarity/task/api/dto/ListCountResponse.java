package com.clarity.task.api.dto;

import java.util.UUID;

public record ListCountResponse(
        UUID listId,
        long activeTaskCount
) {}
