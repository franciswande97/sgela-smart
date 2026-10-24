package com.app.sgelasmart.api.calendar;

import com.app.sgelasmart.api.calendar.dto.CalendarEventRequest;
import com.app.sgelasmart.api.calendar.dto.CalendarEventResponse;
import com.app.sgelasmart.api.calendar.dto.TimetableOcrRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class CalendarController {

    private final CalendarEventService calendarEventService;

    public CalendarController(CalendarEventService calendarEventService) {
        this.calendarEventService = calendarEventService;
    }

    @GetMapping("/users/{user_id}/calendar-events")
    public ResponseEntity<List<CalendarEventResponse>> getEvents(
            @PathVariable("user_id") Long userId,
            Authentication authentication
    ) {
        List<CalendarEventResponse> events = calendarEventService.getEventsByUserId(userId, authentication.getName());
        return ResponseEntity.ok(events);
    }

    @PostMapping("/users/{user_id}/calendar-events")
    public ResponseEntity<CalendarEventResponse> createEvent(
            @PathVariable("user_id") Long userId,
            @Valid @RequestBody CalendarEventRequest requestDto,
            Authentication authentication
    ) {
        CalendarEventResponse createdEvent = calendarEventService.createEvent(userId, requestDto, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }

    @PostMapping(value = "/users/{user_id}/timetable/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<CalendarEventResponse>> uploadTimetable(
            @PathVariable("user_id") Long userId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        List<CalendarEventResponse> events = calendarEventService.uploadAndParseTimetable(userId, file, authentication.getName());
        return ResponseEntity.ok(events);
    }

    @PostMapping("/users/{user_id}/timetable/parse")
    public ResponseEntity<List<CalendarEventResponse>> parseTimetableOcr(
            @PathVariable("user_id") Long userId,
            @Valid @RequestBody TimetableOcrRequest requestDto,
            Authentication authentication
    ) {
        List<CalendarEventResponse> events = calendarEventService.parseTimetableOcr(userId, requestDto.getOcrRawText(), authentication.getName());
        return ResponseEntity.ok(events);
    }
}
