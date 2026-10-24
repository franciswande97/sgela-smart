package com.app.sgelasmart.api.focussession.dto;

import java.time.LocalDateTime;

public class FocusSessionResponse {

    private Long id;
    private Long userId;
    private Long goalId;
    private Long taskId;
    private Integer durationSeconds;
    private Integer completedIntervals;
    private LocalDateTime sessionDate;

    public FocusSessionResponse(Long id, Long userId, Long goalId, Long taskId,
                                Integer durationSeconds, Integer completedIntervals,
                                LocalDateTime sessionDate) {
        this.id = id;
        this.userId = userId;
        this.goalId = goalId;
        this.taskId = taskId;
        this.durationSeconds = durationSeconds;
        this.completedIntervals = completedIntervals;
        this.sessionDate = sessionDate;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getGoalId() {
        return goalId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public Integer getCompletedIntervals() {
        return completedIntervals;
    }

    public LocalDateTime getSessionDate() {
        return sessionDate;
    }
}
