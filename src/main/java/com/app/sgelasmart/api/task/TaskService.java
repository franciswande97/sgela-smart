package com.app.sgelasmart.api.task;
import com.app.sgelasmart.api.goal.dto.QuizQuestionDto;
import com.app.sgelasmart.api.task.dto.QuizSubmissionRequest;
import com.app.sgelasmart.api.task.dto.QuizSubmissionResponse;
import com.app.sgelasmart.api.task.dto.SanitizedQuizQuestionResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.app.sgelasmart.api.auth.UserRepository;
import com.app.sgelasmart.api.goal.GoalRepository;
import com.app.sgelasmart.api.task.dto.TaskRequest;
import com.app.sgelasmart.api.task.dto.TaskResponse;
import com.app.sgelasmart.common.TaskLifecycleState;
import com.app.sgelasmart.common.TaskType;
import com.app.sgelasmart.model.Goal;
import com.app.sgelasmart.model.Task;
import com.app.sgelasmart.model.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final GoalRepository goalRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public TaskService(TaskRepository taskRepository,
                       GoalRepository goalRepository,
                       UserRepository userRepository,
                       ObjectMapper objectMapper) {
        this.taskRepository = taskRepository;
        this.goalRepository = goalRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    public List<SanitizedQuizQuestionResponse> getSanitizedQuiz(Long taskId, String authenticatedEmail) {
        User user = getUserByEmail(authenticatedEmail);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NoSuchElementException("Task not found with id: " + taskId));

        if (!task.getUserId().equals(user.getId())) {
            throw new AccessDeniedException("Forbidden: You do not own this task");
        }

        if (task.getTaskType() != TaskType.QUIZ) {
            throw new IllegalArgumentException("Task with id " + taskId + " is not of type QUIZ");
        }

        List<QuizQuestionDto> questions = parseQuizPayload(task.getContentPayload());

        List<SanitizedQuizQuestionResponse> sanitized = new java.util.ArrayList<>();
        for (int i = 0; i < questions.size(); i++) {
            QuizQuestionDto q = questions.get(i);
            sanitized.add(new SanitizedQuizQuestionResponse(i, q.getQuestion(), q.getOptions()));
        }
        return sanitized;
    }

    public QuizSubmissionResponse submitQuiz(Long taskId, QuizSubmissionRequest submissionRequest, String authenticatedEmail) {
        User user = getUserByEmail(authenticatedEmail);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NoSuchElementException("Task not found with id: " + taskId));

        if (!task.getUserId().equals(user.getId())) {
            throw new AccessDeniedException("Forbidden: You do not own this task");
        }

        if (task.getTaskType() != TaskType.QUIZ) {
            throw new IllegalArgumentException("Task with id " + taskId + " is not of type QUIZ");
        }

        List<QuizQuestionDto> questions = parseQuizPayload(task.getContentPayload());
        if (questions.isEmpty()) {
            throw new IllegalStateException("Quiz content payload is empty or malformed");
        }

        java.util.Map<Integer, Integer> answersMap = new java.util.HashMap<>();
        if (submissionRequest.answers() != null) {
            for (QuizSubmissionRequest.AnswerItem item : submissionRequest.answers()) {
                answersMap.put(item.questionIndex(), item.selectedOptionIndex());
            }
        }

        int score = 0;
        List<QuizSubmissionResponse.QuestionReviewItem> reviewItems = new java.util.ArrayList<>();

        for (int i = 0; i < questions.size(); i++) {
            QuizQuestionDto q = questions.get(i);
            int selected = answersMap.getOrDefault(i, -1);
            boolean isCorrect = (selected == q.getCorrectAnswerIndex());

            if (isCorrect) {
                score++;
            }

            reviewItems.add(new QuizSubmissionResponse.QuestionReviewItem(
                    i,
                    q.getQuestion(),
                    q.getOptions(),
                    selected,
                    q.getCorrectAnswerIndex(),
                    isCorrect,
                    q.getExplanation()
            ));
        }

        int totalQuestions = questions.size();
        double percentage = ((double) score / totalQuestions) * 100.0;
        boolean passed = percentage >= 60.0;

        if (passed) {
            task.setLifecycleState(TaskLifecycleState.COMPLETED);
        } else {
            task.setLifecycleState(TaskLifecycleState.IN_PROGRESS);
        }
        taskRepository.save(task);

        return new QuizSubmissionResponse(
                task.getId(),
                score,
                totalQuestions,
                percentage,
                passed,
                task.getLifecycleState(),
                reviewItems
        );
    }

    private List<QuizQuestionDto> parseQuizPayload(String payloadJson) {
        if (payloadJson == null || payloadJson.isBlank() || "null".equalsIgnoreCase(payloadJson.trim())) {
            return List.of();
        }
        try {
            List<QuizQuestionDto> parsed = objectMapper.readValue(payloadJson, new TypeReference<List<QuizQuestionDto>>() {});
            return parsed != null ? parsed : List.of();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse quiz content payload", e);
        }
    }

    public List<TaskResponse> getTasksByUserId(Long targetUserId, String authenticatedEmail) {
        verifyUserOwnership(targetUserId, authenticatedEmail);

        return taskRepository.findByUserId(targetUserId)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<TaskResponse> getTasksByGoalId(Long goalId, String authenticatedEmail) {
        User user = getUserByEmail(authenticatedEmail);
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new NoSuchElementException("Goal not found with id: " + goalId));

        if (!goal.getUserId().equals(user.getId())) {
            throw new AccessDeniedException("Forbidden: You do not own this goal");
        }

        return taskRepository.findByGoalId(goalId)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public TaskResponse createTask(Long goalId, TaskRequest requestDto, String authenticatedEmail) {
        User user = getUserByEmail(authenticatedEmail);
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new NoSuchElementException("Goal not found with id: " + goalId));

        if (!goal.getUserId().equals(user.getId())) {
            throw new AccessDeniedException("Forbidden: You do not own this goal");
        }

        Task task = new Task();
        task.setGoalId(goalId);
        task.setUserId(user.getId());
        task.setTitle(requestDto.getTitle());
        task.setDescription(requestDto.getDescription());
        task.setTaskType(requestDto.getTaskType() != null ? requestDto.getTaskType() : TaskType.CHECKLIST);
        task.setContentPayload(requestDto.getContentPayload());
        task.setDueDate(requestDto.getDueDate());
        task.setEstimatedDurationMinutes(requestDto.getEstimatedDurationMinutes());
        task.setLifecycleState(requestDto.getLifecycleState() != null ? requestDto.getLifecycleState() : TaskLifecycleState.TODO);

        Task savedTask = taskRepository.save(task);
        return mapToResponseDto(savedTask);
    }

    public TaskResponse updateTask(Long taskId, TaskRequest requestDto, String authenticatedEmail) {
        User user = getUserByEmail(authenticatedEmail);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NoSuchElementException("Task not found with id: " + taskId));

        if (!task.getUserId().equals(user.getId())) {
            throw new AccessDeniedException("Forbidden: You do not own this task");
        }

        task.setTitle(requestDto.getTitle());
        task.setDescription(requestDto.getDescription());
        if (requestDto.getTaskType() != null) {
            task.setTaskType(requestDto.getTaskType());
        }
        if (requestDto.getContentPayload() != null) {
            task.setContentPayload(requestDto.getContentPayload());
        }
        if (requestDto.getLifecycleState() != null) {
            task.setLifecycleState(requestDto.getLifecycleState());
        }
        task.setDueDate(requestDto.getDueDate());
        task.setEstimatedDurationMinutes(requestDto.getEstimatedDurationMinutes());

        Task updatedTask = taskRepository.save(task);
        return mapToResponseDto(updatedTask);
    }

    public void deleteTask(Long taskId, String authenticatedEmail) {
        User user = getUserByEmail(authenticatedEmail);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NoSuchElementException("Task not found with id: " + taskId));

        if (!task.getUserId().equals(user.getId())) {
            throw new AccessDeniedException("Forbidden: You do not own this task");
        }

        taskRepository.delete(task);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AccessDeniedException("User not found or unauthenticated"));
    }

    private void verifyUserOwnership(Long targetUserId, String authenticatedEmail) {
        User user = getUserByEmail(authenticatedEmail);
        if (!user.getId().equals(targetUserId)) {
            throw new AccessDeniedException("Forbidden: You cannot access tasks for another user");
        }
    }

    private TaskResponse mapToResponseDto(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getGoalId(),
                task.getUserId(),
                task.getTitle(),
                task.getDescription(),
                task.getLifecycleState(),
                task.getTaskType(),
                task.getContentPayload(),
                task.getDueDate(),
                task.getEstimatedDurationMinutes(),
                task.getCreatedAt(),
                task.getScheduledDate(),
                task.getStartTime(),
                task.getEndTime()
        );
    }
}
