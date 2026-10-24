package com.app.sgelasmart.api.goal;

import com.app.sgelasmart.model.GoalMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoalMaterialRepository extends JpaRepository<GoalMaterial, Long> {

    List<GoalMaterial> findByGoalIdOrderByCreatedAtDesc(Long goalId);

    void deleteByGoalId(Long goalId);
}
