package com.app.sgelasmart.api.calendar;

import com.app.sgelasmart.common.EventSource;
import com.app.sgelasmart.model.CalendarEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {

    List<CalendarEvent> findByUserId(Long userId);

    void deleteByUserIdAndSource(Long userId, EventSource source);
}
