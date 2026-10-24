package com.app.sgelasmart.api.schedule;

import com.app.sgelasmart.api.schedule.dto.ScheduleItemResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping("/users/{user_id}/schedule")
    public ResponseEntity<List<ScheduleItemResponse>> getDailySchedule(
            @PathVariable("user_id") Long userId,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication
    ) {
        LocalDate queryDate = (date != null) ? date : LocalDate.now();
        List<ScheduleItemResponse> schedule = scheduleService.getDailySchedule(userId, queryDate, authentication.getName());
        return ResponseEntity.ok(schedule);
    }
}
