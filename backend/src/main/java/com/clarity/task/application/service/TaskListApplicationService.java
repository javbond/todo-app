package com.clarity.task.application.service;

import com.clarity.task.application.command.CreateListCommand;
import com.clarity.task.application.command.UpdateListCommand;
import com.clarity.task.application.query.TaskListQuery;
import com.clarity.task.domain.model.TaskList;
import com.clarity.task.domain.repository.TaskListRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Application service for TaskList use cases.
 */
@Service
@Transactional
public class TaskListApplicationService {

    private final TaskListRepository taskListRepository;

    public TaskListApplicationService(TaskListRepository taskListRepository) {
        this.taskListRepository = taskListRepository;
    }

    public TaskList createList(CreateListCommand command) {
        // TODO: Implement in Sprint 2 — create list, check name uniqueness
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public TaskList updateList(UpdateListCommand command) {
        // TODO: Implement in Sprint 2 — load list, rename/recolor, save
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void deleteList(UUID listId, UUID userId) {
        // TODO: Implement in Sprint 2 — load list, ensureDeletable, reassign tasks, delete
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Transactional(readOnly = true)
    public List<TaskList> getListsForUser(TaskListQuery query) {
        // TODO: Implement in Sprint 2 — query all lists for user
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
