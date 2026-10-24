package com.app.sgelasmart.api.task.dto;

import java.util.List;

public record QuizSubmissionRequest(
        List<AnswerItem> answers
) {
    public record AnswerItem(
            int questionIndex,
            int selectedOptionIndex
    ) {}
}
