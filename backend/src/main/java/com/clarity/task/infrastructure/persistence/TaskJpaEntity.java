package com.clarity.task.infrastructure.persistence;

import com.clarity.shared.domain.AuditableEntity;
import com.clarity.task.domain.model.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
public class TaskJpaEntity extends AuditableEntity {

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "priority", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private Priority priority;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "reminder_interval", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private ReminderInterval reminderInterval;

    @Column(name = "is_completed", nullable = false)
    private boolean completed;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    @Column(name = "is_today", nullable = false)
    private boolean today;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "list_id", nullable = false)
    private UUID listId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    // ─── Conversion ───────────────────────────────────────────────────────────

    public Task toDomain() {
        DueDate domainDueDate = (this.dueDate != null) ? DueDate.of(this.dueDate) : null;
        return Task.builder()
                .id(this.getId())
                .title(this.title)
                .description(this.description)
                .priority(this.priority)
                .dueDate(domainDueDate)
                .reminderInterval(this.reminderInterval)
                .completed(this.completed)
                .deleted(this.deleted)
                .today(this.today)
                .completedAt(this.completedAt)
                .sortOrder(this.sortOrder)
                .listId(this.listId)
                .userId(this.userId)
                .createdAt(this.getCreatedAt())
                .updatedAt(this.getUpdatedAt())
                .version(this.getVersion() != null ? this.getVersion() : 0)
                .build();
    }

    public static TaskJpaEntity fromDomain(Task task) {
        TaskJpaEntity entity = new TaskJpaEntity();
        entity.setId(task.getId());
        entity.setTitle(task.getTitle());
        entity.setDescription(task.getDescription());
        entity.setPriority(task.getPriority());
        entity.setDueDate(task.getDueDate() != null ? task.getDueDate().getValue() : null);
        entity.setReminderInterval(task.getReminderInterval());
        entity.setCompleted(task.isCompleted());
        entity.setDeleted(task.isDeleted());
        entity.setToday(task.isToday());
        entity.setCompletedAt(task.getCompletedAt());
        entity.setSortOrder(task.getSortOrder());
        entity.setListId(task.getListId());
        entity.setUserId(task.getUserId());
        entity.setVersion(task.getVersion());
        return entity;
    }
}
