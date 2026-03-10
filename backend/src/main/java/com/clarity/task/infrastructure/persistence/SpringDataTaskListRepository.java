package com.clarity.task.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataTaskListRepository extends JpaRepository<TaskListJpaEntity, UUID> {

    Optional<TaskListJpaEntity> findByIdAndUserId(UUID id, UUID userId);

    Optional<TaskListJpaEntity> findByUserIdAndDefaultListTrue(UUID userId);

    List<TaskListJpaEntity> findAllByUserIdOrderBySortOrder(UUID userId);

    boolean existsByNameAndUserId(String name, UUID userId);
}
