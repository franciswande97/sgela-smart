package com.app.sgelasmart.api.goal;

import com.app.sgelasmart.api.auth.UserRepository;
import com.app.sgelasmart.api.goal.dto.GoalRequest;
import com.app.sgelasmart.api.goal.dto.GoalResponse;
import com.app.sgelasmart.common.GoalStatus;
import com.app.sgelasmart.model.Goal;
import com.app.sgelasmart.model.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoalService {

    private final GoalRepository goalRepository;
    private final UserRepository userRepository;

    public GoalService(GoalRepository goalRepository, UserRepository userRepository) {
        this.goalRepository = goalRepository;
        this.userRepository = userRepository;
    }

    public List<GoalResponse> getGoalsByUserId(Long userId, String authenticatedEmail) {
        verifyUserOwnership(userId, authenticatedEmail);

        return goalRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public GoalResponse createGoal(Long userId, GoalRequest requestDto, String authenticatedEmail) {
        verifyUserOwnership(userId, authenticatedEmail);

        Goal goal = new Goal();
        goal.setUserId(userId);
        goal.setStatus(GoalStatus.NOT_STARTED);
        goal.setTitle(requestDto.getTitle());
        goal.setDeadline(requestDto.getDeadline());

        Goal savedGoal = goalRepository.save(goal);
        return mapToResponseDto(savedGoal);
    }

    private void verifyUserOwnership(Long targetUserId, String authenticatedEmail) {
        User user = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new AccessDeniedException("User not found or unauthenticated"));

        if (!user.getId().equals(targetUserId)) {
            throw new AccessDeniedException("Forbidden: You cannot access or modify goals for another user");
        }
    }

    private GoalResponse mapToResponseDto(Goal goal) {
        return new GoalResponse(
                goal.getId(),
                goal.getUserId(),
                goal.getTitle(),
                goal.getDeadline(),
                goal.getStatus(),
                goal.getCreatedAt()
        );
    }
}
