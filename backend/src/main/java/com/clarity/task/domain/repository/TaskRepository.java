package com.clarity.task.domain.repository;

import com.clarity.task.domain.model.Task;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port: Task repository interface (domain layer).
 * Implemented by the infrastructure persistence adapter.
 */
public interface TaskRepository {

    Task save(Task task);

    Optional<Task> findById(UUID id);

    Optional<Task> findByIdAndUserId(UUID id, UUID userId);

    List<Task> findAllByListId(UUID listId);

    List<Task> findActiveByUserId(UUID userId);

    List<Task> findActiveByListIdAndUserId(UUID listId, UUID userId);

    List<Task> findTodayViewByUserId(UUID userId);

    long countActiveByListIdAndUserId(UUID listId, UUID userId);

    void delete(Task task);
}
