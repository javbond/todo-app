package com.clarity.task.domain.exception;

import java.util.UUID;

public class ListNotFoundException extends RuntimeException {

    public ListNotFoundException(UUID listId) {
        super("Task list not found: " + listId);
    }

    public ListNotFoundException(String message) {
        super(message);
    }
}
