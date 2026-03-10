package com.clarity.task.api.dto;

import java.util.List;

public record TodayViewResponse(
        List<TaskResponse> overdue,
        List<TaskResponse> today,
        List<TaskResponse> completedToday
) {}
