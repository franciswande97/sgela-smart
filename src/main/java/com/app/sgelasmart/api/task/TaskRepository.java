package com.app.sgelasmart.api.task;
import java.time.LocalDate;

import com.app.sgelasmart.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUserId(Long userId);
    List<Task> findByGoalId(Long goalId);
    List<Task> findByUserIdAndScheduledDate(Long userId, LocalDate scheduledDate);
}
