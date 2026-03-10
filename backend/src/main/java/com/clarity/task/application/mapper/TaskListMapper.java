package com.clarity.task.application.mapper;

import com.clarity.task.api.dto.TaskListResponse;
import com.clarity.task.domain.model.TaskList;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TaskListMapper {

    TaskListResponse toResponse(TaskList taskList);

    List<TaskListResponse> toResponseList(List<TaskList> lists);
}
