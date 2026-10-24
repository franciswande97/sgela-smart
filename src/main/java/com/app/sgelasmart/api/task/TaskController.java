package com.app.sgelasmart.api.task;
import com.app.sgelasmart.api.task.dto.QuizSubmissionRequest;
import com.app.sgelasmart.api.task.dto.QuizSubmissionResponse;
import com.app.sgelasmart.api.task.dto.SanitizedQuizQuestionResponse;
import com.app.sgelasmart.api.task.dto.TaskRequest;
import com.app.sgelasmart.api.task.dto.TaskResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/tasks/{task_id}/quiz")
    public ResponseEntity<List<SanitizedQuizQuestionResponse>> getQuizQuestions(
            @PathVariable("task_id") Long taskId,
            Authentication authentication
    ) {
        List<SanitizedQuizQuestionResponse> questions = taskService.getSanitizedQuiz(taskId, authentication.getName());
        return ResponseEntity.ok(questions);
    }

    @PostMapping("/tasks/{task_id}/submit-quiz")
    public ResponseEntity<QuizSubmissionResponse> submitQuiz(
            @PathVariable("task_id") Long taskId,
            @RequestBody QuizSubmissionRequest submissionRequest,
            Authentication authentication
    ) {
        QuizSubmissionResponse result = taskService.submitQuiz(taskId, submissionRequest, authentication.getName());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/users/{user_id}/tasks")
    public ResponseEntity<List<TaskResponse>> getAllTasks(
            @PathVariable("user_id") Long userId,
            Authentication authentication
    ) {
        List<TaskResponse> tasks = taskService.getTasksByUserId(userId, authentication.getName());
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/goals/{goal_id}/tasks")
    public ResponseEntity<List<TaskResponse>> getTasksByGoal(
            @PathVariable("goal_id") Long goalId,
            Authentication authentication
    ) {
        List<TaskResponse> tasks = taskService.getTasksByGoalId(goalId, authentication.getName());
        return ResponseEntity.ok(tasks);
    }

    @PostMapping("/goals/{goal_id}/tasks")
    public ResponseEntity<TaskResponse> createTask(
            @PathVariable("goal_id") Long goalId,
            @Valid @RequestBody TaskRequest requestDto,
            Authentication authentication
    ) {
        TaskResponse createdTask = taskService.createTask(goalId, requestDto, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    @PutMapping("/tasks/{task_id}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable("task_id") Long taskId,
            @Valid @RequestBody TaskRequest requestDto,
            Authentication authentication
    ) {
        TaskResponse updatedTask = taskService.updateTask(taskId, requestDto, authentication.getName());
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/tasks/{task_id}")
    public ResponseEntity<Map<String, Object>> deleteTask(
            @PathVariable("task_id") Long taskId,
            Authentication authentication
    ) {
        taskService.deleteTask(taskId, authentication.getName());
        return ResponseEntity.ok(Map.of("message", "Task deleted successfully", "task_id", taskId));
    }
}
