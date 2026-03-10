package com.clarity.task.application.query;

import java.util.UUID;

public record TaskQuery(
        UUID userId,
        UUID listId
) {}
