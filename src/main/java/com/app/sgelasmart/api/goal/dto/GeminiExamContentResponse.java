package com.app.sgelasmart.api.goal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class GeminiExamContentResponse {

    @JsonProperty("quiz_questions")
    private List<QuizQuestionDto> quizQuestions;

    @JsonProperty("external_prompt")
    private ExternalPromptDto externalPrompt;

    public GeminiExamContentResponse() {}

    public GeminiExamContentResponse(List<QuizQuestionDto> quizQuestions, ExternalPromptDto externalPrompt) {
        this.quizQuestions = quizQuestions;
        this.externalPrompt = externalPrompt;
    }

    public List<QuizQuestionDto> getQuizQuestions() {
        return quizQuestions;
    }

    public void setQuizQuestions(List<QuizQuestionDto> quizQuestions) {
        this.quizQuestions = quizQuestions;
    }

    public ExternalPromptDto getExternalPrompt() {
        return externalPrompt;
    }

    public void setExternalPrompt(ExternalPromptDto externalPrompt) {
        this.externalPrompt = externalPrompt;
    }
}
