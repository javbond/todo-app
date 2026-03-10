package com.clarity.task.domain.exception;

public class TaskTitleRequiredException extends RuntimeException {

    public TaskTitleRequiredException(String message) {
        super(message);
    }
}
