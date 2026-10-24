package com.app.sgelasmart.api.goal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ExternalPromptDto {

    @JsonProperty("recommended_engine")
    private String recommendedEngine;

    @JsonProperty("prompt_text")
    private String promptText;

    public ExternalPromptDto() {}

    public ExternalPromptDto(String recommendedEngine, String promptText) {
        this.recommendedEngine = recommendedEngine;
        this.promptText = promptText;
    }

    public String getRecommendedEngine() {
        return recommendedEngine;
    }

    public void setRecommendedEngine(String recommendedEngine) {
        this.recommendedEngine = recommendedEngine;
    }

    public String getPromptText() {
        return promptText;
    }

    public void setPromptText(String promptText) {
        this.promptText = promptText;
    }
}
