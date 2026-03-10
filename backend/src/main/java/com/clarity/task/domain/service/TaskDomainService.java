package com.clarity.task.domain.service;

import com.clarity.task.domain.model.Task;
import com.clarity.task.domain.model.TaskList;
import com.clarity.task.domain.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Domain service for task business logic that spans multiple aggregates.
 */
@Service
public class TaskDomainService {

    private final TaskRepository taskRepository;

    public TaskDomainService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * Reassign all tasks from a deleted list to the inbox list.
     */
    public void reassignTasksToInbox(UUID deletedListId, TaskList inboxList) {
        List<Task> tasks = taskRepository.findAllByListId(deletedListId);
        for (Task task : tasks) {
            task.moveToList(inboxList.getId());
            taskRepository.save(task);
        }
    }

    /**
     * Count active tasks for a given user in a given list.
     */
    public long countActiveTasksInList(UUID listId, UUID userId) {
        return taskRepository.countActiveByListIdAndUserId(listId, userId);
    }
}
