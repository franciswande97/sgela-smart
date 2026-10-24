package com.app.sgelasmart.api.focussession;

import com.app.sgelasmart.model.FocusSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FocusSessionRepository extends JpaRepository<FocusSession, Long> {
    List<FocusSession> findByUserId(Long userId);
    List<FocusSession> findByGoalId(Long goalId);
}
