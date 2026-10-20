package com.app.sgelasmart.api.goal.dto;

import com.app.sgelasmart.common.GoalStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class GoalResponse {
    private Long id;
    private Long userId;
    private String title;
    private LocalDate deadline;
    private GoalStatus status;
    private LocalDateTime createdAt;

    public GoalResponse(Long id, Long userId, String title, LocalDate deadline, GoalStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.deadline = deadline;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public GoalStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
