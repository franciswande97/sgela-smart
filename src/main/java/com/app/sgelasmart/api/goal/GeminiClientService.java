package com.app.sgelasmart.api.goal;

import com.app.sgelasmart.api.goal.dto.ExternalPromptDto;
import com.app.sgelasmart.api.goal.dto.GeminiExamContentResponse;
import com.app.sgelasmart.api.goal.dto.QuizQuestionDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GeminiClientService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key:}")
    private String apiKey;

    public GeminiClientService(ObjectMapper objectMapper) {
        this.restClient = RestClient.builder().build();
        this.objectMapper = objectMapper;
    }

    public GeminiExamContentResponse generateExamContent(List<String> topics, String notesSnippet) {
        if (apiKey == null || apiKey.isBlank()) {
            return generateMockContent(topics);
        }

        String prompt = "Generate up to 6 multiple choice active-recall quiz questions and one tailored external LLM prompt " +
                "for an exam covering these topics: " + String.join(", ", topics) + ". " +
                (notesSnippet != null && !notesSnippet.isBlank() ? "Ground questions strictly in these study notes: " + notesSnippet : "");

        Map<String, Object> questionSchema = Map.of(
                "type", "OBJECT",
                "properties", Map.of(
                        "question", Map.of("type", "STRING"),
                        "options", Map.of("type", "ARRAY", "items", Map.of("type", "STRING")),
                        "correct_answer_index", Map.of("type", "INTEGER"),
                        "explanation", Map.of("type", "STRING")
                ),
                "required", List.of("question", "options", "correct_answer_index", "explanation")
        );

        Map<String, Object> externalPromptSchema = Map.of(
                "type", "OBJECT",
                "properties", Map.of(
                        "recommended_engine", Map.of("type", "STRING"),
                        "prompt_text", Map.of("type", "STRING")
                ),
                "required", List.of("recommended_engine", "prompt_text")
        );

        Map<String, Object> responseSchema = Map.of(
                "type", "OBJECT",
                "properties", Map.of(
                        "quiz_questions", Map.of("type", "ARRAY", "items", questionSchema),
                        "external_prompt", externalPromptSchema
                ),
                "required", List.of("quiz_questions", "external_prompt")
        );

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                "generationConfig", Map.of(
                        "response_mime_type", "application/json",
                        "response_schema", responseSchema
                )
        );

        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;

        try {
            String responseJson = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, resp) -> {
                        throw new ResponseStatusException(HttpStatusCode.valueOf(503), "Gemini AI service unavailable");
                    })
                    .body(String.class);

            JsonNode root = objectMapper.readTree(responseJson);
            String rawJsonText = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
            return objectMapper.readValue(rawJsonText, GeminiExamContentResponse.class);
        } catch (Exception e) {
            return generateMockContent(topics);
        }
    }

    private GeminiExamContentResponse generateMockContent(List<String> topics) {
        List<QuizQuestionDto> questions = new ArrayList<>();
        for (int i = 0; i < Math.min(topics.size(), 6); i++) {
            String topic = topics.get(i);
            questions.add(new QuizQuestionDto(
                    "What is a primary concept behind " + topic + "?",
                    List.of("Core theoretical principle", "Secondary implementation detail", "Deprecated approach", "Irrelevant factor"),
                    0,
                    "This diagnostic question tests the foundational definition of " + topic + "."
            ));
        }

        ExternalPromptDto prompt = new ExternalPromptDto(
                "Claude 3.7 Sonnet",
                "Explain the most complex edge cases and problem-solving steps for " +
                        (topics.isEmpty() ? "these topics" : topics.get(0)) + " with a step-by-step worked example."
        );

        return new GeminiExamContentResponse(questions, prompt);
    }
}
