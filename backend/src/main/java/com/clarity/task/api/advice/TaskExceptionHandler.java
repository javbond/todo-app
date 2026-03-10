package com.clarity.task.api.advice;

import com.clarity.shared.exception.ErrorResponse;
import com.clarity.task.domain.exception.InboxUndeletableException;
import com.clarity.task.domain.exception.ListNotFoundException;
import com.clarity.task.domain.exception.TaskNotFoundException;
import com.clarity.task.domain.exception.TaskTitleRequiredException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TaskExceptionHandler {

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTaskNotFound(TaskNotFoundException ex,
                                                              HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(404, "Not Found", ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(ListNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleListNotFound(ListNotFoundException ex,
                                                              HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(404, "Not Found", ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(InboxUndeletableException.class)
    public ResponseEntity<ErrorResponse> handleInboxUndeletable(InboxUndeletableException ex,
                                                                  HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ErrorResponse.of(422, "Business Rule Violation", ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(TaskTitleRequiredException.class)
    public ResponseEntity<ErrorResponse> handleTitleRequired(TaskTitleRequiredException ex,
                                                               HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(400, "Bad Request", ex.getMessage(), request.getRequestURI()));
    }
}
