package com.app.sgelasmart.api.goal;

import com.app.sgelasmart.api.ai.StudyIntelligenceEngine;
import com.app.sgelasmart.api.auth.UserRepository;
import com.app.sgelasmart.api.goal.dto.ApplyTemplateRequest;
import com.app.sgelasmart.api.goal.dto.GeminiExamContentResponse;
import com.app.sgelasmart.api.goal.dto.GoalResponse;
import com.app.sgelasmart.api.goal.dto.GoalTemplateResponse;
import com.app.sgelasmart.api.task.TaskRepository;
import com.app.sgelasmart.api.task.dto.TaskResponse;
import com.app.sgelasmart.common.GoalStatus;
import com.app.sgelasmart.common.TaskLifecycleState;
import com.app.sgelasmart.common.TaskType;
import com.app.sgelasmart.model.Goal;
import com.app.sgelasmart.model.GoalMaterial;
import com.app.sgelasmart.model.Task;
import com.app.sgelasmart.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import com.app.sgelasmart.api.schedule.TimetableVoidService;
import com.app.sgelasmart.api.schedule.dto.TimeSlot;

@Service
public class ExamPrepTemplateService {

    private final GoalRepository goalRepository;
    private final GoalMaterialRepository goalMaterialRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final StudyIntelligenceEngine intelligenceEngine;
    private final ObjectMapper objectMapper;
    private final TimetableVoidService voidService;

    public ExamPrepTemplateService(GoalRepository goalRepository,
                                   GoalMaterialRepository goalMaterialRepository,
                                   TaskRepository taskRepository,
                                   UserRepository userRepository,
                                   StudyIntelligenceEngine intelligenceEngine,
                                   ObjectMapper objectMapper,
                                   TimetableVoidService voidService) {
        this.goalRepository = goalRepository;
        this.goalMaterialRepository = goalMaterialRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.intelligenceEngine = intelligenceEngine;
        this.objectMapper = objectMapper;
        this.voidService = voidService;
    }

    @Transactional
    public GoalTemplateResponse applyExamPrepTemplate(Long goalId, ApplyTemplateRequest request, String authenticatedEmail) {
        User user = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new AccessDeniedException("User not authenticated"));

        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new NoSuchElementException("Goal not found with id: " + goalId));

        if (!goal.getUserId().equals(user.getId())) {
            throw new AccessDeniedException("Forbidden: You do not own this goal");
        }

        LocalDate now = LocalDate.now();
        LocalDate deadline = goal.getDeadline() != null ? goal.getDeadline() : now.plusDays(14);
        long totalDays = Math.max(3, ChronoUnit.DAYS.between(now, deadline));

        long masteryEndDay = Math.max(1, Math.round(totalDays * 0.40));
        long practiceEndDay = Math.max(masteryEndDay + 1, Math.round(totalDays * 0.80));

        List<Task> generatedTasks = new ArrayList<>();
        List<String> topics = request.getTopics();

        // 1. Concept Mastery Phase (40%): FOCUS_SESSION tasks per topic
        for (int i = 0; i < topics.size(); i++) {
            String topic = topics.get(i);
            long offset = Math.min(masteryEndDay, (i + 1) * masteryEndDay / Math.max(1, topics.size()));
            LocalDate dueDate = now.plusDays(offset);

            String payload = String.format("{\"targetDurationMinutes\":50,\"intervalMinutes\":25,\"breakMinutes\":5,\"objective\":\"Deep reading and core notes on: %s\"}", topic);

            generatedTasks.add(new Task(
                    goal.getId(),
                    user.getId(),
                    "Mastery Session: " + topic,
                    "Read key material, establish mental models, and draft summaries for " + topic + ".",
                    TaskLifecycleState.TODO,
                    TaskType.FOCUS_SESSION,
                    payload,
                    dueDate,
                    50
            ));
        }

        // 2. Active Recall & Practice Phase (40%): Enforce grounding material prerequisite
        List<GoalMaterial> materials = goalMaterialRepository.findByGoalIdOrderByCreatedAtDesc(goal.getId());
        String aggregatedCorpus = materials.stream()
                .filter(m -> m.getExtractedContent() != null && !m.getExtractedContent().isBlank())
                .map(m -> m.getFileName() + ": " + m.getExtractedContent())
                .collect(Collectors.joining("\n\n"));

        if (aggregatedCorpus.isBlank() && (request.getStudyNotesSnippet() != null && !request.getStudyNotesSnippet().isBlank())) {
            aggregatedCorpus = request.getStudyNotesSnippet();
        }

        if (aggregatedCorpus == null || aggregatedCorpus.isBlank()) {
            throw new IllegalArgumentException(
                    "Exam Prep template requires at least one uploaded material bucket file or a non-empty studyNotesSnippet to ground the exam generation."
            );
        }


        GeminiExamContentResponse contentResponse = intelligenceEngine.generateExamContent(topics, aggregatedCorpus);

        // Clean sweep stale/previous tasks for this goal upon successful AI generation
        List<Task> existingTasks = taskRepository.findByGoalId(goal.getId());
        if (!existingTasks.isEmpty()) {
            taskRepository.deleteAll(existingTasks);
            taskRepository.flush();
        }


        String quizPayloadJson = "[]";
        try {
            quizPayloadJson = objectMapper.writeValueAsString(contentResponse.getQuizQuestions());
        } catch (JsonProcessingException ignored) {}

        generatedTasks.add(new Task(
                goal.getId(),
                user.getId(),
                "Active Recall Challenge: " + goal.getTitle(),
                "Interactive test evaluating core exam concepts.",
                TaskLifecycleState.TODO,
                TaskType.QUIZ,
                quizPayloadJson,
                now.plusDays(masteryEndDay + 1),
                20
        ));

        String promptPayloadJson = "{}";
        try {
            promptPayloadJson = objectMapper.writeValueAsString(contentResponse.getExternalPrompt());
        } catch (JsonProcessingException ignored) {}

        generatedTasks.add(new Task(
                goal.getId(),
                user.getId(),
                "Deep Problem-Solving Prompt",
                "Advanced scenario and edge case practice using an external assistant.",
                TaskLifecycleState.TODO,
                TaskType.EXTERNAL_PROMPT,
                promptPayloadJson,
                now.plusDays(practiceEndDay),
                30
        ));

        // 3. Mock Simulation & Review Phase (20%): FOCUS_SESSION mock and CHECKLIST
        generatedTasks.add(new Task(
                goal.getId(),
                user.getId(),
                "Timed Mock Exam Simulation",
                "Simulate actual exam conditions with a 90-minute uninterrupted session.",
                TaskLifecycleState.TODO,
                TaskType.FOCUS_SESSION,
                "{\"targetDurationMinutes\":90,\"intervalMinutes\":90,\"breakMinutes\":0,\"objective\":\"Simulate exam conditions with timed past papers.\"}",
                deadline.minusDays(1),
                90
        ));

        generatedTasks.add(new Task(
                goal.getId(),
                user.getId(),
                "Exam Logistics & Equipment Check",
                "Check venue location, verify student ID card, and pack approved pens/calculators.",
                TaskLifecycleState.TODO,
                TaskType.CHECKLIST,
                null,
                deadline,
                15
        ));

        // -------------------------------------------------------------------
        // PUT BLOCK B HERE (Right before saving to taskRepository)
        // -------------------------------------------------------------------
        List<com.app.sgelasmart.api.schedule.dto.TimeSlot> availableVoids = 
                new ArrayList<>(voidService.findFreeVoids(user.getId(), now, deadline));

        int totalRequiredMinutes = generatedTasks.stream()
                .mapToInt(t -> t.getEstimatedDurationMinutes() != null ? t.getEstimatedDurationMinutes() : 30)
                .sum();

        int totalAvailableMinutes = availableVoids.stream()
                .mapToInt(com.app.sgelasmart.api.schedule.dto.TimeSlot::durationMinutes)
                .sum();

        double scaleFactor = (totalAvailableMinutes > 0 && totalAvailableMinutes < totalRequiredMinutes)
                ? (double) totalAvailableMinutes / totalRequiredMinutes
                : 1.0;

        for (Task task : generatedTasks) {
            int originalDuration = task.getEstimatedDurationMinutes() != null ? task.getEstimatedDurationMinutes() : 30;
            int scaledDuration = Math.max(15, (int) Math.round(originalDuration * scaleFactor));
            task.setEstimatedDurationMinutes(scaledDuration);

            com.app.sgelasmart.api.schedule.dto.TimeSlot matchedVoid = null;
            int matchedIndex = -1;

            for (int j = 0; j < availableVoids.size(); j++) {
                com.app.sgelasmart.api.schedule.dto.TimeSlot slot = availableVoids.get(j);
                if (slot.durationMinutes() >= scaledDuration) {
                    matchedVoid = slot;
                    matchedIndex = j;
                    break;
                }
            }

            if (matchedVoid != null) {
                task.setScheduledDate(matchedVoid.date());
                task.setStartTime(matchedVoid.startTime());
                task.setEndTime(matchedVoid.startTime().plusMinutes(scaledDuration));

                int remainingMinutes = matchedVoid.durationMinutes() - scaledDuration;
                if (remainingMinutes >= 20) {
                    availableVoids.set(matchedIndex, new com.app.sgelasmart.api.schedule.dto.TimeSlot(
                            matchedVoid.date(),
                            matchedVoid.startTime().plusMinutes(scaledDuration),
                            matchedVoid.endTime(),
                            remainingMinutes
                    ));
                } else {
                    availableVoids.remove(matchedIndex);
                }
            } else {
                task.setScheduledDate(task.getDueDate());
            }
        }
        // -------------------------------------------------------------------

        List<Task> savedTasks = taskRepository.saveAll(generatedTasks);

        goal.setStatus(GoalStatus.IN_PROGRESS);
        Goal savedGoal = goalRepository.save(goal);

        // -------------------------------------------------------------------
        // PUT BLOCK C HERE (Replaces the old taskResponses mapping)
        // -------------------------------------------------------------------
        List<TaskResponse> taskResponses = savedTasks.stream()
                .map(t -> new TaskResponse(
                        t.getId(), t.getGoalId(), t.getUserId(), t.getTitle(), t.getDescription(),
                        t.getLifecycleState(), t.getTaskType(), t.getContentPayload(),
                        t.getDueDate(), t.getEstimatedDurationMinutes(), t.getCreatedAt(),
                        t.getScheduledDate(), t.getStartTime(), t.getEndTime()
                ))
                .collect(Collectors.toList());

        GoalResponse goalResponse = new GoalResponse(
                savedGoal.getId(), savedGoal.getUserId(), savedGoal.getTitle(),
                savedGoal.getDeadline(), savedGoal.getStatus(), savedGoal.getCreatedAt()
        );

        return new GoalTemplateResponse(goalResponse, taskResponses);
    }
}
