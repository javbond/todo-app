package com.clarity.task.application.service;

import com.clarity.task.application.command.*;
import com.clarity.task.application.query.TaskQuery;
import com.clarity.task.application.query.TodayTaskQuery;
import com.clarity.task.domain.model.Task;
import com.clarity.task.domain.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Application service for Task use cases.
 * Orchestrates domain logic and repository access.
 */
@Service
@Transactional
public class TaskApplicationService {

    private final TaskRepository taskRepository;

    public TaskApplicationService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Task createTask(CreateTaskCommand command) {
        // TODO: Implement in Sprint 2 — create task, publish TaskCreatedEvent
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public Task updateTask(UpdateTaskCommand command) {
        // TODO: Implement in Sprint 2 — load task, call updateDetails, save
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public Task completeTask(CompleteTaskCommand command) {
        // TODO: Implement in Sprint 2 — load task, call complete/uncomplete, save
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void deleteTask(UUID taskId, UUID userId) {
        // TODO: Implement in Sprint 2 — load task, call softDelete, save
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void toggleToday(UUID taskId, UUID userId) {
        // TODO: Implement in Sprint 2 — load task, call toggleToday, save
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Transactional(readOnly = true)
    public List<Task> getTasksByList(TaskQuery query) {
        // TODO: Implement in Sprint 2 — query tasks by list
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Transactional(readOnly = true)
    public List<Task> getTodayView(TodayTaskQuery query) {
        // TODO: Implement in Sprint 2 — query today view tasks
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
