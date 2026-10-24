package com.app.sgelasmart.api.focussession;

import com.app.sgelasmart.api.focussession.dto.FocusSessionRequest;
import com.app.sgelasmart.api.focussession.dto.FocusSessionResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users/{user_id}/focus-sessions")
public class FocusSessionController {

    private final FocusSessionService focusSessionService;

    public FocusSessionController(FocusSessionService focusSessionService) {
        this.focusSessionService = focusSessionService;
    }

    @GetMapping
    public ResponseEntity<List<FocusSessionResponse>> getFocusSessions(
            @PathVariable("user_id") Long userId,
            Authentication authentication
    ) {
        List<FocusSessionResponse> sessions = focusSessionService.getFocusSessionsByUserId(userId, authentication.getName());
        return ResponseEntity.ok(sessions);
    }

    @PostMapping
    public ResponseEntity<FocusSessionResponse> createFocusSession(
            @PathVariable("user_id") Long userId,
            @Valid @RequestBody FocusSessionRequest requestDto,
            Authentication authentication
    ) {
        FocusSessionResponse createdSession = focusSessionService.createFocusSession(userId, requestDto, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSession);
    }
}
