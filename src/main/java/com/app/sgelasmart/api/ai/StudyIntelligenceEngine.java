package com.app.sgelasmart.api.ai;

import com.app.sgelasmart.api.goal.dto.ExternalPromptDto;
import com.app.sgelasmart.api.goal.dto.GeminiExamContentResponse;
import com.app.sgelasmart.api.goal.dto.QuizQuestionDto;
import com.app.sgelasmart.api.goal.dto.TimetableSlotDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class StudyIntelligenceEngine {

    private static final Logger log = LoggerFactory.getLogger(StudyIntelligenceEngine.class);
    private final RestClient restClient;
    private static final String MODEL_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash-lite:generateContent?key=";
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key:}")
    private String apiKey;

    public StudyIntelligenceEngine(ObjectMapper objectMapper) {
        this.restClient = RestClient.builder().build();
        this.objectMapper = objectMapper;
    }

    public List<TimetableSlotDto> parseTimetable(byte[] fileBytes, String mimeType) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("GEMINI_API_KEY is not configured. Falling back to mock timetable.");
            return generateMockTimetable();
        }

        String base64Data = Base64.getEncoder().encodeToString(fileBytes);
        String prompt = "Extract all recurring weekly class, tutorial, and lecture blocks from this schedule. " +
                "Return every block with title, dayOfWeek (MONDAY to SUNDAY), startTime (HH:mm), and endTime (HH:mm).";

        Map<String, Object> timetableSchema = Map.of(
                "type", "ARRAY",
                "items", Map.of(
                        "type", "OBJECT",
                        "properties", Map.of(
                                "title", Map.of("type", "STRING"),
                                "dayOfWeek", Map.of("type", "STRING", "enum", List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY")),
                                "startTime", Map.of("type", "STRING"),
                                "endTime", Map.of("type", "STRING")
                        ),
                        "required", List.of("title", "dayOfWeek", "startTime", "endTime")
                )
        );

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(
                                Map.of("inlineData", Map.of("mimeType", mimeType, "data", base64Data)),
                                Map.of("text", prompt)
                        )
                )),
                "generationConfig", Map.of(
                        "responseMimeType", "application/json",
                        "responseSchema", timetableSchema
                )
        );

        try {
            log.info("Sending timetable ({} bytes, MIME: {}) to Gemini...", fileBytes.length, mimeType);
            String rawJsonText = executeGeminiCall(requestBody);
            log.info("Gemini raw JSON response: {}", rawJsonText);
            return objectMapper.readValue(rawJsonText, new TypeReference<List<TimetableSlotDto>>() {});
        } catch (Exception e) {
            log.error("Failed to parse timetable via Gemini API. Error: {}", e.getMessage(), e);
            return generateMockTimetable();
        }
    }

    public String extractMaterialContent(byte[] fileBytes, String mimeType) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("GEMINI_API_KEY is not configured. Falling back to mock material extraction.");
            return "Extracted summary of foundational lecture concepts and definitions.";
        }

        String base64Data = Base64.getEncoder().encodeToString(fileBytes);
        String prompt = "Extract all key definitions, theorems, core concepts, and formulas from this study material. " +
                "Provide a comprehensive, high-yield study summary that can be used to generate practice exam questions.";

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(
                                Map.of("inlineData", Map.of("mimeType", mimeType, "data", base64Data)),
                                Map.of("text", prompt)
                        )
                ))
        );

        try {
            log.info("Sending document ({} bytes, MIME: {}) to Gemini...", fileBytes.length, mimeType);
            String text = executeGeminiCall(requestBody);
            log.info("Document extracted successfully.");
            return text;
        } catch (Exception e) {
            log.error("Failed to extract material content via Gemini. Error: {}", e.getMessage(), e);
            return "Extracted summary of foundational lecture concepts and definitions.";
        }
    }

    public GeminiExamContentResponse generateExamContent(List<String> topics, String aggregatedNotes) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("GEMINI_API_KEY is not configured. Falling back to mock exam content.");
            return generateMockExamContent(topics);
        }

        String prompt = "Generate up to 6 multiple choice active-recall quiz questions and one tailored external LLM prompt " +
                "for an exam covering these topics: " + String.join(", ", topics) + ". " +
                (aggregatedNotes != null && !aggregatedNotes.isBlank() ? "Ground questions strictly in these study notes: " + aggregatedNotes : "");

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
                        "responseMimeType", "application/json",
                        "responseSchema", responseSchema
                )
        );

        try {
            log.info("Generating exam content for topics: {}", topics);
            String rawJsonText = executeGeminiCall(requestBody);
            return objectMapper.readValue(rawJsonText, GeminiExamContentResponse.class);
        } catch (Exception e) {
            log.error("Failed to generate exam content via Gemini. Error: {}", e.getMessage(), e);
            return generateMockExamContent(topics);
        }
    }

    private String executeGeminiCall(Map<String, Object> requestBody) throws Exception {
        String url = MODEL_URL + apiKey;

        String responseJson = restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, resp) -> {
                    throw new ResponseStatusException(HttpStatusCode.valueOf(503), "AI Intelligence Engine unavailable: " + resp.getStatusCode());
                })
                .body(String.class);

        JsonNode root = objectMapper.readTree(responseJson);
        return root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
    }

    private List<TimetableSlotDto> generateMockTimetable() {
        return List.of(
                new TimetableSlotDto("Core Lecture 1", "MONDAY", "09:00", "11:00"),
                new TimetableSlotDto("Tutorial Lab", "WEDNESDAY", "14:00", "16:00"),
                new TimetableSlotDto("Core Lecture 2", "FRIDAY", "10:00", "12:00")
        );
    }

    private GeminiExamContentResponse generateMockExamContent(List<String> topics) {
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
