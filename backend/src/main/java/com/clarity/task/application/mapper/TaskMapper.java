package com.clarity.task.application.mapper;

import com.clarity.task.api.dto.TaskResponse;
import com.clarity.task.domain.model.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    @Mapping(target = "priority", source = "priority")
    @Mapping(target = "dueDate", expression = "java(task.getDueDate() != null ? task.getDueDate().getValue() : null)")
    TaskResponse toResponse(Task task);

    List<TaskResponse> toResponseList(List<Task> tasks);
}
