package com.app.sgelasmart.api.goal.dto;

import com.app.sgelasmart.api.task.dto.TaskResponse;
import java.util.List;

public class GoalTemplateResponse {

    private GoalResponse goal;
    private List<TaskResponse> generatedTasks;

    public GoalTemplateResponse() {}

    public GoalTemplateResponse(GoalResponse goal, List<TaskResponse> generatedTasks) {
        this.goal = goal;
        this.generatedTasks = generatedTasks;
    }

    public GoalResponse getGoal() {
        return goal;
    }

    public void setGoal(GoalResponse goal) {
        this.goal = goal;
    }

    public List<TaskResponse> getGeneratedTasks() {
        return generatedTasks;
    }

    public void setGeneratedTasks(List<TaskResponse> generatedTasks) {
        this.generatedTasks = generatedTasks;
    }
}
