package com.app.sgelasmart.api.schedule.dto;

import java.time.LocalTime;

public record BusyInterval(
        LocalTime startTime,
        LocalTime endTime,
        String source
) implements Comparable<BusyInterval> {

    @Override
    public int compareTo(BusyInterval other) {
        return this.startTime.compareTo(other.startTime);
    }
}
