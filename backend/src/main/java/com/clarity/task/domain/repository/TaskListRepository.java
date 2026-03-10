package com.clarity.task.domain.repository;

import com.clarity.task.domain.model.TaskList;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port: TaskList repository interface (domain layer).
 * Implemented by the infrastructure persistence adapter.
 */
public interface TaskListRepository {

    TaskList save(TaskList taskList);

    Optional<TaskList> findById(UUID id);

    Optional<TaskList> findByIdAndUserId(UUID id, UUID userId);

    Optional<TaskList> findDefaultByUserId(UUID userId);

    List<TaskList> findAllByUserId(UUID userId);

    boolean existsByNameAndUserId(String name, UUID userId);

    void delete(TaskList taskList);
}
