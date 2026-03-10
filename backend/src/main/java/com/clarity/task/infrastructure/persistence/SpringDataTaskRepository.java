package com.clarity.task.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataTaskRepository extends JpaRepository<TaskJpaEntity, UUID> {

    Optional<TaskJpaEntity> findByIdAndUserId(UUID id, UUID userId);

    List<TaskJpaEntity> findAllByListId(UUID listId);

    @Query("SELECT t FROM TaskJpaEntity t WHERE t.userId = :userId AND t.deleted = false AND t.completed = false")
    List<TaskJpaEntity> findActiveByUserId(@Param("userId") UUID userId);

    @Query("SELECT t FROM TaskJpaEntity t WHERE t.listId = :listId AND t.userId = :userId AND t.deleted = false AND t.completed = false")
    List<TaskJpaEntity> findActiveByListIdAndUserId(@Param("listId") UUID listId, @Param("userId") UUID userId);

    @Query("SELECT t FROM TaskJpaEntity t WHERE t.userId = :userId AND t.deleted = false AND t.completed = false AND (t.today = true OR t.dueDate IS NOT NULL)")
    List<TaskJpaEntity> findTodayViewByUserId(@Param("userId") UUID userId);

    @Query("SELECT COUNT(t) FROM TaskJpaEntity t WHERE t.listId = :listId AND t.userId = :userId AND t.deleted = false AND t.completed = false")
    long countActiveByListIdAndUserId(@Param("listId") UUID listId, @Param("userId") UUID userId);
}
