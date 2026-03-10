package com.clarity.task.application.command;

import java.util.UUID;

public record CompleteTaskCommand(
        UUID taskId,
        UUID userId,
        boolean completed
) {}
