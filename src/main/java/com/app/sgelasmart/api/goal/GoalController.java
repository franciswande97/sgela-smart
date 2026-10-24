package com.app.sgelasmart.api.goal;

import com.app.sgelasmart.api.goal.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class GoalController {

    private final GoalService goalService;
    private final GoalMaterialService goalMaterialService;
    private final ExamPrepTemplateService examPrepTemplateService;

    public GoalController(GoalService goalService,
                          GoalMaterialService goalMaterialService,
                          ExamPrepTemplateService examPrepTemplateService) {
        this.goalService = goalService;
        this.goalMaterialService = goalMaterialService;
        this.examPrepTemplateService = examPrepTemplateService;
    }

    @GetMapping("/users/{user_id}/goals")
    public ResponseEntity<List<GoalResponse>> getAllGoals(
            @PathVariable("user_id") Long userId,
            Authentication authentication
    ) {
        List<GoalResponse> goals = goalService.getGoalsByUserId(userId, authentication.getName());
        return ResponseEntity.ok(goals);
    }

    @PostMapping("/users/{user_id}/goals")
    public ResponseEntity<GoalResponse> createGoal(
            @PathVariable("user_id") Long userId,
            @Valid @RequestBody GoalRequest requestDto,
            Authentication authentication
    ) {
        GoalResponse createdGoal = goalService.createGoal(userId, requestDto, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdGoal);
    }

    @PostMapping(value = "/goals/{goal_id}/materials/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GoalMaterialResponse> uploadMaterial(
            @PathVariable("goal_id") Long goalId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        GoalMaterialResponse response = goalMaterialService.uploadDocument(goalId, file, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/goals/{goal_id}/materials/text")
    public ResponseEntity<GoalMaterialResponse> addTextMaterial(
            @PathVariable("goal_id") Long goalId,
            @Valid @RequestBody MaterialTextRequest requestDto,
            Authentication authentication
    ) {
        GoalMaterialResponse response = goalMaterialService.addTextMaterial(goalId, requestDto, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/goals/{goal_id}/materials")
    public ResponseEntity<List<GoalMaterialResponse>> getMaterials(
            @PathVariable("goal_id") Long goalId,
            Authentication authentication
    ) {
        List<GoalMaterialResponse> materials = goalMaterialService.getMaterialsByGoal(goalId, authentication.getName());
        return ResponseEntity.ok(materials);
    }

    @PostMapping("/goals/{goal_id}/apply-template")
    public ResponseEntity<GoalTemplateResponse> applyTemplate(
            @PathVariable("goal_id") Long goalId,
            @Valid @RequestBody ApplyTemplateRequest requestDto,
            Authentication authentication
    ) {
        GoalTemplateResponse response = examPrepTemplateService.applyExamPrepTemplate(goalId, requestDto, authentication.getName());
        return ResponseEntity.ok(response);
    }
}
