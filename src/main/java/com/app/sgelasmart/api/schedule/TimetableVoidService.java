package com.app.sgelasmart.api.schedule;

import com.app.sgelasmart.api.calendar.CalendarEventRepository;
import com.app.sgelasmart.api.schedule.dto.BusyInterval;
import com.app.sgelasmart.api.schedule.dto.TimeSlot;
import com.app.sgelasmart.api.task.TaskRepository;
import com.app.sgelasmart.model.CalendarEvent;
import com.app.sgelasmart.model.Task;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class TimetableVoidService {

    private final CalendarEventRepository calendarEventRepository;
    private final TaskRepository taskRepository;

    private static final LocalTime DAY_START = LocalTime.of(7, 30);
    private static final LocalTime DAY_END = LocalTime.of(21, 30);
    private static final int MIN_USABLE_GAP_MINUTES = 20;

    public TimetableVoidService(CalendarEventRepository calendarEventRepository, TaskRepository taskRepository) {
        this.calendarEventRepository = calendarEventRepository;
        this.taskRepository = taskRepository;
    }

    public List<TimeSlot> findFreeVoids(Long userId, LocalDate startDate, LocalDate endDate) {
        List<TimeSlot> freeVoids = new ArrayList<>();
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            freeVoids.addAll(findFreeVoidsForDate(userId, currentDate));
            currentDate = currentDate.plusDays(1);
        }
        return freeVoids;
    }

    public List<TimeSlot> findFreeVoidsForDate(Long userId, LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();

        List<CalendarEvent> allEvents = calendarEventRepository.findByUserId(userId);
        List<Task> allTasks = taskRepository.findByUserId(userId);

        List<BusyInterval> rawIntervals = new ArrayList<>();

        for (CalendarEvent e : allEvents) {
            if (e.getStartTime() == null || e.getEndTime() == null) continue;
            boolean matchesRecurring = Boolean.TRUE.equals(e.getIsRecurring()) && dow.equals(e.getDayOfWeek());
            boolean matchesDate = date.equals(e.getEventDate());
            if (matchesRecurring || matchesDate) {
                rawIntervals.add(new BusyInterval(e.getStartTime(), e.getEndTime(), "CALENDAR_EVENT"));
            }
        }

        for (Task t : allTasks) {
            if (t.getStartTime() == null || t.getEndTime() == null) continue;
            if (date.equals(t.getScheduledDate())) {
                rawIntervals.add(new BusyInterval(t.getStartTime(), t.getEndTime(), "SCHEDULED_TASK"));
            }
        }

        if (rawIntervals.isEmpty()) {
            int totalDayMinutes = (int) Duration.between(DAY_START, DAY_END).toMinutes();
            return List.of(new TimeSlot(date, DAY_START, DAY_END, totalDayMinutes));
        }

        Collections.sort(rawIntervals);

        List<BusyInterval> merged = new ArrayList<>();
        BusyInterval current = rawIntervals.get(0);

        for (int i = 1; i < rawIntervals.size(); i++) {
            BusyInterval next = rawIntervals.get(i);
            if (!next.startTime().isAfter(current.endTime())) {
                LocalTime maxEnd = next.endTime().isAfter(current.endTime()) ? next.endTime() : current.endTime();
                current = new BusyInterval(current.startTime(), maxEnd, "MERGED");
            } else {
                merged.add(current);
                current = next;
            }
        }
        merged.add(current);

        List<TimeSlot> dailyVoids = new ArrayList<>();
        LocalTime pointer = DAY_START;

        for (BusyInterval interval : merged) {
            if (interval.startTime().isAfter(pointer)) {
                int gapMinutes = (int) Duration.between(pointer, interval.startTime()).toMinutes();
                if (gapMinutes >= MIN_USABLE_GAP_MINUTES) {
                    dailyVoids.add(new TimeSlot(date, pointer, interval.startTime(), gapMinutes));
                }
            }
            if (interval.endTime().isAfter(pointer)) {
                pointer = interval.endTime();
            }
        }

        if (pointer.isBefore(DAY_END)) {
            int finalGapMinutes = (int) Duration.between(pointer, DAY_END).toMinutes();
            if (finalGapMinutes >= MIN_USABLE_GAP_MINUTES) {
                dailyVoids.add(new TimeSlot(date, pointer, DAY_END, finalGapMinutes));
            }
        }

        return dailyVoids;
    }
}
