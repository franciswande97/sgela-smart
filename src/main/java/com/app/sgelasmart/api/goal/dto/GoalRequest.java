package com.app.sgelasmart.api.goal.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public class GoalRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @FutureOrPresent(message = "Deadline cannot be in the past")
    private LocalDate deadline;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }
}
