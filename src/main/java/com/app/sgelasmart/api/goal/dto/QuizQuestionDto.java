package com.app.sgelasmart.api.goal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class QuizQuestionDto {
    private String question;
    private List<String> options;

    @JsonProperty("correct_answer_index")
    private Integer correctAnswerIndex;

    private String explanation;

    public QuizQuestionDto() {}

    public QuizQuestionDto(String question, List<String> options, Integer correctAnswerIndex, String explanation) {
        this.question = question;
        this.options = options;
        this.correctAnswerIndex = correctAnswerIndex;
        this.explanation = explanation;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public Integer getCorrectAnswerIndex() {
        return correctAnswerIndex;
    }

    public void setCorrectAnswerIndex(Integer correctAnswerIndex) {
        this.correctAnswerIndex = correctAnswerIndex;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
