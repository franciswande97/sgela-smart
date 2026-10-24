package com.app.sgelasmart.api.schedule;

import com.app.sgelasmart.api.auth.UserRepository;
import com.app.sgelasmart.api.calendar.CalendarEventRepository;
import com.app.sgelasmart.api.schedule.dto.ScheduleItemResponse;
import com.app.sgelasmart.api.schedule.dto.TimeSlot;
import com.app.sgelasmart.api.task.TaskRepository;
import com.app.sgelasmart.model.CalendarEvent;
import com.app.sgelasmart.model.Task;
import com.app.sgelasmart.model.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ScheduleService {

    private final CalendarEventRepository calendarEventRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TimetableVoidService voidService;

    public ScheduleService(CalendarEventRepository calendarEventRepository,
                           TaskRepository taskRepository,
                           UserRepository userRepository,
                           TimetableVoidService voidService) {
        this.calendarEventRepository = calendarEventRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.voidService = voidService;
    }

    public List<ScheduleItemResponse> getDailySchedule(Long targetUserId, LocalDate date, String authenticatedEmail) {
        User user = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new AccessDeniedException("User not authenticated"));

        if (!user.getId().equals(targetUserId)) {
            throw new AccessDeniedException("Forbidden: You cannot access another user's schedule");
        }

        List<ScheduleItemResponse> timeline = new ArrayList<>();
        DayOfWeek dow = date.getDayOfWeek();

        // 1. Ingest classes & calendar events
        List<CalendarEvent> allEvents = calendarEventRepository.findByUserId(targetUserId);
        for (CalendarEvent event : allEvents) {
            if (event.getStartTime() == null || event.getEndTime() == null) continue;

            boolean matchesRecurring = Boolean.TRUE.equals(event.getIsRecurring()) && dow.equals(event.getDayOfWeek());
            boolean matchesDate = date.equals(event.getEventDate());

            if (matchesRecurring || matchesDate) {
                int duration = (int) Duration.between(event.getStartTime(), event.getEndTime()).toMinutes();
                String itemType = Boolean.TRUE.equals(event.getIsRecurring()) ? "CLASS" : "EVENT";
                String subtitle = event.getSource() != null ? event.getSource().name() : null;

                timeline.add(new ScheduleItemResponse(
                        itemType,
                        event.getId(),
                        event.getTitle(),
                        subtitle,
                        date,
                        event.getStartTime(),
                        event.getEndTime(),
                        duration,
                        false,
                        null,
                        null
                ));
            }
        }

        // 2. Ingest scheduled study tasks
        List<Task> allTasks = taskRepository.findByUserId(targetUserId);
        for (Task task : allTasks) {
            if (date.equals(task.getScheduledDate()) && task.getStartTime() != null && task.getEndTime() != null) {
                int duration = (int) Duration.between(task.getStartTime(), task.getEndTime()).toMinutes();

                timeline.add(new ScheduleItemResponse(
                        "STUDY_TASK",
                        task.getId(),
                        task.getTitle(),
                        task.getDescription(),
                        date,
                        task.getStartTime(),
                        task.getEndTime(),
                        duration,
                        true,
                        task.getTaskType(),
                        task.getLifecycleState()
                ));
            }
        }

        // 3. Interleave detected free windows
        List<TimeSlot> voids = voidService.findFreeVoidsForDate(targetUserId, date);
        for (TimeSlot slot : voids) {
            timeline.add(new ScheduleItemResponse(
                    "FREE_WINDOW",
                    null,
                    "Free Window",
                    slot.durationMinutes() + " mins available",
                    date,
                    slot.startTime(),
                    slot.endTime(),
                    slot.durationMinutes(),
                    false,
                    null,
                    null
            ));
        }

        // 4. Sort chronologically by startTime
        Collections.sort(timeline);

        return timeline;
    }
}
