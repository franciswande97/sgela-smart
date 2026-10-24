package com.app.sgelasmart.api.task.dto;

import java.util.List;

public record SanitizedQuizQuestionResponse(
        int questionIndex,
        String question,
        List<String> options
) {}
