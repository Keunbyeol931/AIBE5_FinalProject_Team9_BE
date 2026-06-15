package com.grimgate.grimgate_backend.domain.minigame.repository;

import com.grimgate.grimgate_backend.domain.minigame.entity.MinigameStageAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MinigameStageAttemptRepository extends JpaRepository<MinigameStageAttempt, Long> {

    long countByPlayIdAndStageNo(Long playId, Integer stageNo);
}
