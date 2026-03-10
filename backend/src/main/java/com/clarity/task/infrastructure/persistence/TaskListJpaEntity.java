package com.clarity.task.infrastructure.persistence;

import com.clarity.shared.domain.AuditableEntity;
import com.clarity.task.domain.model.ListColor;
import com.clarity.task.domain.model.TaskList;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "task_lists")
@Getter
@Setter
@NoArgsConstructor
public class TaskListJpaEntity extends AuditableEntity {

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "color", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ListColor color;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "is_default", nullable = false)
    private boolean defaultList;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    // ─── Conversion ───────────────────────────────────────────────────────────

    public TaskList toDomain() {
        return TaskList.builder()
                .id(this.getId())
                .name(this.name)
                .color(this.color)
                .sortOrder(this.sortOrder)
                .defaultList(this.defaultList)
                .userId(this.userId)
                .createdAt(this.getCreatedAt())
                .updatedAt(this.getUpdatedAt())
                .version(this.getVersion() != null ? this.getVersion() : 0)
                .build();
    }

    public static TaskListJpaEntity fromDomain(TaskList taskList) {
        TaskListJpaEntity entity = new TaskListJpaEntity();
        entity.setId(taskList.getId());
        entity.setName(taskList.getName());
        entity.setColor(taskList.getColor());
        entity.setSortOrder(taskList.getSortOrder());
        entity.setDefaultList(taskList.isDefaultList());
        entity.setUserId(taskList.getUserId());
        entity.setVersion(taskList.getVersion());
        return entity;
    }
}
