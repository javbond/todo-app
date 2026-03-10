package com.clarity.task.infrastructure.persistence;

import com.clarity.task.domain.model.TaskList;
import com.clarity.task.domain.repository.TaskListRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter: JPA implementation of TaskListRepository (domain port).
 */
@Repository
public class JpaTaskListRepository implements TaskListRepository {

    private final SpringDataTaskListRepository springDataTaskListRepository;

    public JpaTaskListRepository(SpringDataTaskListRepository springDataTaskListRepository) {
        this.springDataTaskListRepository = springDataTaskListRepository;
    }

    @Override
    public TaskList save(TaskList taskList) {
        TaskListJpaEntity entity = TaskListJpaEntity.fromDomain(taskList);
        TaskListJpaEntity saved = springDataTaskListRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<TaskList> findById(UUID id) {
        return springDataTaskListRepository.findById(id).map(TaskListJpaEntity::toDomain);
    }

    @Override
    public Optional<TaskList> findByIdAndUserId(UUID id, UUID userId) {
        return springDataTaskListRepository.findByIdAndUserId(id, userId).map(TaskListJpaEntity::toDomain);
    }

    @Override
    public Optional<TaskList> findDefaultByUserId(UUID userId) {
        return springDataTaskListRepository.findByUserIdAndDefaultListTrue(userId).map(TaskListJpaEntity::toDomain);
    }

    @Override
    public List<TaskList> findAllByUserId(UUID userId) {
        return springDataTaskListRepository.findAllByUserIdOrderBySortOrder(userId).stream()
                .map(TaskListJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByNameAndUserId(String name, UUID userId) {
        return springDataTaskListRepository.existsByNameAndUserId(name, userId);
    }

    @Override
    public void delete(TaskList taskList) {
        springDataTaskListRepository.findById(taskList.getId()).ifPresent(springDataTaskListRepository::delete);
    }
}
