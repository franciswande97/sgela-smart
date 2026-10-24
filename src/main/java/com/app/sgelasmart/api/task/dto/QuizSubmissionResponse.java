package com.app.sgelasmart.api.task.dto;

import com.app.sgelasmart.common.TaskLifecycleState;
import java.util.List;

public record QuizSubmissionResponse(
        Long taskId,
        int score,
        int totalQuestions,
        double percentage,
        boolean passed,
        TaskLifecycleState lifecycleState,
        List<QuestionReviewItem> review
) {
    public record QuestionReviewItem(
            int questionIndex,
            String question,
            List<String> options,
            int selectedOptionIndex,
            int correctAnswerIndex,
            boolean isCorrect,
            String explanation
    ) {}
}
