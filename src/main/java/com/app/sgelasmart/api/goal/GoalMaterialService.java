package com.app.sgelasmart.api.goal;

import com.app.sgelasmart.api.ai.StudyIntelligenceEngine;
import com.app.sgelasmart.api.auth.UserRepository;
import com.app.sgelasmart.api.goal.dto.GoalMaterialResponse;
import com.app.sgelasmart.api.goal.dto.MaterialTextRequest;
import com.app.sgelasmart.model.Goal;
import com.app.sgelasmart.model.GoalMaterial;
import com.app.sgelasmart.model.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class GoalMaterialService {

    private final GoalMaterialRepository goalMaterialRepository;
    private final GoalRepository goalRepository;
    private final UserRepository userRepository;
    private final StudyIntelligenceEngine intelligenceEngine;

    public GoalMaterialService(GoalMaterialRepository goalMaterialRepository,
                               GoalRepository goalRepository,
                               UserRepository userRepository,
                               StudyIntelligenceEngine intelligenceEngine) {
        this.goalMaterialRepository = goalMaterialRepository;
        this.goalRepository = goalRepository;
        this.userRepository = userRepository;
        this.intelligenceEngine = intelligenceEngine;
    }

    @Transactional
    public GoalMaterialResponse uploadDocument(Long goalId, MultipartFile file, String authenticatedEmail) {
        Goal goal = verifyGoalOwnership(goalId, authenticatedEmail);

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file cannot be empty");
        }

        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "study_document";

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read uploaded file contents", e);
        }

        String extractedContent = intelligenceEngine.extractMaterialContent(bytes, contentType);

        GoalMaterial material = new GoalMaterial(goal.getId(), fileName, contentType, extractedContent);
        GoalMaterial saved = goalMaterialRepository.save(material);

        return mapToResponse(saved);
    }

    @Transactional
    public GoalMaterialResponse addTextMaterial(Long goalId, MaterialTextRequest request, String authenticatedEmail) {
        Goal goal = verifyGoalOwnership(goalId, authenticatedEmail);

        GoalMaterial material = new GoalMaterial(
                goal.getId(),
                request.getTitle(),
                "text/plain",
                request.getContent()
        );
        GoalMaterial saved = goalMaterialRepository.save(material);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<GoalMaterialResponse> getMaterialsByGoal(Long goalId, String authenticatedEmail) {
        verifyGoalOwnership(goalId, authenticatedEmail);
        return goalMaterialRepository.findByGoalIdOrderByCreatedAtDesc(goalId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private Goal verifyGoalOwnership(Long goalId, String authenticatedEmail) {
        User user = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new AccessDeniedException("User not authenticated"));

        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new NoSuchElementException("Goal not found with id: " + goalId));

        if (!goal.getUserId().equals(user.getId())) {
            throw new AccessDeniedException("Forbidden: You do not own this goal");
        }

        return goal;
    }

    private GoalMaterialResponse mapToResponse(GoalMaterial material) {
        String summary = material.getExtractedContent();
        if (summary != null && summary.length() > 200) {
            summary = summary.substring(0, 197) + "...";
        }
        return new GoalMaterialResponse(
                material.getId(),
                material.getGoalId(),
                material.getFileName(),
                material.getContentType(),
                summary,
                material.getCreatedAt()
        );
    }
}
