package com.clarity.task.domain.exception;

public class InboxUndeletableException extends RuntimeException {

    public InboxUndeletableException(String message) {
        super(message);
    }
}
