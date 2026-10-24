package com.app.sgelasmart.api.calendar.dto;

import com.app.sgelasmart.common.EventSource;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

public class CalendarEventResponse {

    private Long id;
    private Long userId;
    private String title;
    private DayOfWeek dayOfWeek;
    private LocalDate eventDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private EventSource source;
    private Boolean isRecurring;

    public CalendarEventResponse(Long id, Long userId, String title, DayOfWeek dayOfWeek,
                                 LocalDate eventDate, LocalTime startTime, LocalTime endTime,
                                 EventSource source, Boolean isRecurring) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.dayOfWeek = dayOfWeek;
        this.eventDate = eventDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.source = source;
        this.isRecurring = isRecurring;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public EventSource getSource() {
        return source;
    }

    public Boolean getIsRecurring() {
        return isRecurring;
    }
}
