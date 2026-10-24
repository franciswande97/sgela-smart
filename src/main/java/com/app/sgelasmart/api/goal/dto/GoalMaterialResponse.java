package com.app.sgelasmart.api.goal.dto;

import java.time.LocalDateTime;

public class GoalMaterialResponse {

    private Long id;
    private Long goalId;
    private String fileName;
    private String contentType;
    private String extractedContentSummary;
    private LocalDateTime createdAt;

    public GoalMaterialResponse() {}

    public GoalMaterialResponse(Long id, Long goalId, String fileName, String contentType, String extractedContentSummary, LocalDateTime createdAt) {
        this.id = id;
        this.goalId = goalId;
        this.fileName = fileName;
        this.contentType = contentType;
        this.extractedContentSummary = extractedContentSummary;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGoalId() {
        return goalId;
    }

    public void setGoalId(Long goalId) {
        this.goalId = goalId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getExtractedContentSummary() {
        return extractedContentSummary;
    }

    public void setExtractedContentSummary(String extractedContentSummary) {
        this.extractedContentSummary = extractedContentSummary;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
