package com.app.sgelasmart.api.goal;

import com.app.sgelasmart.api.goal.dto.GoalRequest;
import com.app.sgelasmart.api.goal.dto.GoalResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users/{user_id}/goals")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @GetMapping
    public ResponseEntity<List<GoalResponse>> getAllGoals(
            @PathVariable("user_id") Long userId,
            Authentication authentication
    ) {
        List<GoalResponse> goals = goalService.getGoalsByUserId(userId, authentication.getName());
        return ResponseEntity.ok(goals);
    }

    @PostMapping
    public ResponseEntity<GoalResponse> createGoal(
            @PathVariable("user_id") Long userId,
            @Valid @RequestBody GoalRequest requestDto,
            Authentication authentication
    ) {
        GoalResponse createdGoal = goalService.createGoal(userId, requestDto, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdGoal);
    }
}
