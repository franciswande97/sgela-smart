package com.app.sgelasmart.api.schedule.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record TimeSlot(
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        int durationMinutes
) {}
