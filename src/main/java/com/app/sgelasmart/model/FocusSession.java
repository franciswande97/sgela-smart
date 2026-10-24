package com.app.sgelasmart.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "focus_sessions")
public class FocusSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "goal_id")
    private Long goalId;

    @Column(name = "task_id")
    private Long taskId;

    @Column(name = "duration_seconds", nullable = false)
    private Integer durationSeconds;

    @Column(name = "completed_intervals", nullable = false)
    private Integer completedIntervals;

    @Column(name = "session_date", nullable = false)
    private LocalDateTime sessionDate = LocalDateTime.now();

    public FocusSession() {}

    public FocusSession(Long userId, Long goalId, Long taskId, Integer durationSeconds, Integer completedIntervals) {
        this.userId = userId;
        this.goalId = goalId;
        this.taskId = taskId;
        this.durationSeconds = durationSeconds;
        this.completedIntervals = completedIntervals;
        this.sessionDate = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getGoalId() {
        return goalId;
    }

    public void setGoalId(Long goalId) {
        this.goalId = goalId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public Integer getCompletedIntervals() {
        return completedIntervals;
    }

    public void setCompletedIntervals(Integer completedIntervals) {
        this.completedIntervals = completedIntervals;
    }

    public LocalDateTime getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(LocalDateTime sessionDate) {
        this.sessionDate = sessionDate;
    }
}
