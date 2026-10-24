package com.app.sgelasmart.api.schedule.dto;

import com.app.sgelasmart.common.TaskLifecycleState;
import com.app.sgelasmart.common.TaskType;

import java.time.LocalDate;
import java.time.LocalTime;

public record ScheduleItemResponse(
        String itemType,          // "CLASS", "EVENT", "STUDY_TASK", "FREE_WINDOW"
        Long referenceId,         // CalendarEvent ID or Task ID (null for FREE_WINDOW)
        String title,
        String subtitle,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        int durationMinutes,
        boolean isStudyTask,
        TaskType taskType,
        TaskLifecycleState lifecycleState
) implements Comparable<ScheduleItemResponse> {

    @Override
    public int compareTo(ScheduleItemResponse other) {
        if (this.startTime == null && other.startTime == null) return 0;
        if (this.startTime == null) return 1;
        if (other.startTime == null) return -1;
        return this.startTime.compareTo(other.startTime);
    }
}
