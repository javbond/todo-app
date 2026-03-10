package com.clarity.task.infrastructure.persistence;

import com.clarity.task.domain.model.Task;
import com.clarity.task.domain.repository.TaskRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter: JPA implementation of TaskRepository (domain port).
 */
@Repository
public class JpaTaskRepository implements TaskRepository {

    private final SpringDataTaskRepository springDataTaskRepository;

    public JpaTaskRepository(SpringDataTaskRepository springDataTaskRepository) {
        this.springDataTaskRepository = springDataTaskRepository;
    }

    @Override
    public Task save(Task task) {
        TaskJpaEntity entity = TaskJpaEntity.fromDomain(task);
        TaskJpaEntity saved = springDataTaskRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<Task> findById(UUID id) {
        return springDataTaskRepository.findById(id).map(TaskJpaEntity::toDomain);
    }

    @Override
    public Optional<Task> findByIdAndUserId(UUID id, UUID userId) {
        return springDataTaskRepository.findByIdAndUserId(id, userId).map(TaskJpaEntity::toDomain);
    }

    @Override
    public List<Task> findAllByListId(UUID listId) {
        return springDataTaskRepository.findAllByListId(listId).stream()
                .map(TaskJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> findActiveByUserId(UUID userId) {
        return springDataTaskRepository.findActiveByUserId(userId).stream()
                .map(TaskJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> findActiveByListIdAndUserId(UUID listId, UUID userId) {
        return springDataTaskRepository.findActiveByListIdAndUserId(listId, userId).stream()
                .map(TaskJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> findTodayViewByUserId(UUID userId) {
        return springDataTaskRepository.findTodayViewByUserId(userId).stream()
                .map(TaskJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countActiveByListIdAndUserId(UUID listId, UUID userId) {
        return springDataTaskRepository.countActiveByListIdAndUserId(listId, userId);
    }

    @Override
    public void delete(Task task) {
        springDataTaskRepository.findById(task.getId()).ifPresent(springDataTaskRepository::delete);
    }
}
