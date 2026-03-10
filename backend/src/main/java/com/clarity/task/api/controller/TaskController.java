package com.clarity.task.api.controller;

import com.clarity.task.api.dto.*;
import com.clarity.task.application.service.TaskApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskApplicationService taskApplicationService;

    public TaskController(TaskApplicationService taskApplicationService) {
        this.taskApplicationService = taskApplicationService;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest request) {
        // TODO: Implement in Sprint 2 — extract userId from security context
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @GetMapping
    public ResponseEntity<?> getTasksByList(@RequestParam UUID listId) {
        // TODO: Implement in Sprint 2
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @GetMapping("/today")
    public ResponseEntity<TodayViewResponse> getTodayView() {
        // TODO: Implement in Sprint 2
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable UUID taskId,
                                                    @Valid @RequestBody UpdateTaskRequest request) {
        // TODO: Implement in Sprint 2
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @PatchMapping("/{taskId}/complete")
    public ResponseEntity<TaskResponse> completeTask(@PathVariable UUID taskId,
                                                      @RequestParam boolean completed) {
        // TODO: Implement in Sprint 2
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @PatchMapping("/{taskId}/today")
    public ResponseEntity<TaskResponse> toggleToday(@PathVariable UUID taskId) {
        // TODO: Implement in Sprint 2
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @DeleteMapping("/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable UUID taskId) {
        // TODO: Implement in Sprint 2
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
