package com.clarity.task.api.controller;

import com.clarity.task.api.dto.*;
import com.clarity.task.application.service.TaskListApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/lists")
public class TaskListController {

    private final TaskListApplicationService taskListApplicationService;

    public TaskListController(TaskListApplicationService taskListApplicationService) {
        this.taskListApplicationService = taskListApplicationService;
    }

    @PostMapping
    public ResponseEntity<TaskListResponse> createList(@Valid @RequestBody CreateListRequest request) {
        // TODO: Implement in Sprint 2 — extract userId from security context
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @GetMapping
    public ResponseEntity<List<TaskListResponse>> getLists() {
        // TODO: Implement in Sprint 2
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @PutMapping("/{listId}")
    public ResponseEntity<TaskListResponse> updateList(@PathVariable UUID listId,
                                                        @Valid @RequestBody UpdateListRequest request) {
        // TODO: Implement in Sprint 2
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @DeleteMapping("/{listId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteList(@PathVariable UUID listId) {
        // TODO: Implement in Sprint 2
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @GetMapping("/counts")
    public ResponseEntity<List<ListCountResponse>> getListCounts() {
        // TODO: Implement in Sprint 2
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
