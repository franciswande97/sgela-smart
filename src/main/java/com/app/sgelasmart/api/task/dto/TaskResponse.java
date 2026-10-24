package com.app.sgelasmart.api.task.dto;

import com.app.sgelasmart.common.TaskLifecycleState;
import com.app.sgelasmart.common.TaskType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class TaskResponse {

    private Long id;
    private Long goalId;
    private Long userId;
    private String title;
    private String description;
    private TaskLifecycleState lifecycleState;
    private TaskType taskType;
    private String contentPayload;
    private LocalDate dueDate;
    private Integer estimatedDurationMinutes;
    private LocalDateTime createdAt;
    private LocalDate scheduledDate;
    private LocalTime startTime;
    private LocalTime endTime;

    // 11-arg constructor for existing callers (TaskService)
    public TaskResponse(Long id, Long goalId, Long userId, String title, String description,
                        TaskLifecycleState lifecycleState, TaskType taskType, String contentPayload,
                        LocalDate dueDate, Integer estimatedDurationMinutes, LocalDateTime createdAt) {
        this(id, goalId, userId, title, description, lifecycleState, taskType, contentPayload,
             dueDate, estimatedDurationMinutes, createdAt, null, null, null);
    }

    // 14-arg constructor with scheduling fields
    public TaskResponse(Long id, Long goalId, Long userId, String title, String description,
                        TaskLifecycleState lifecycleState, TaskType taskType, String contentPayload,
                        LocalDate dueDate, Integer estimatedDurationMinutes, LocalDateTime createdAt,
                        LocalDate scheduledDate, LocalTime startTime, LocalTime endTime) {
        this.id = id;
        this.goalId = goalId;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.lifecycleState = lifecycleState;
        this.taskType = taskType;
        this.contentPayload = contentPayload;
        this.dueDate = dueDate;
        this.estimatedDurationMinutes = estimatedDurationMinutes;
        this.createdAt = createdAt;
        this.scheduledDate = scheduledDate;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Long getId() { return id; }
    public Long getGoalId() { return goalId; }
    public Long getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public TaskLifecycleState getLifecycleState() { return lifecycleState; }
    public TaskType getTaskType() { return taskType; }
    public String getContentPayload() { return contentPayload; }
    public LocalDate getDueDate() { return dueDate; }
    public Integer getEstimatedDurationMinutes() { return estimatedDurationMinutes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDate getScheduledDate() { return scheduledDate; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
}
