package com.grimgate.grimgate_backend.domain.minigame.repository;

import com.grimgate.grimgate_backend.domain.minigame.entity.MinigameStage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MinigameStageRepository extends JpaRepository<MinigameStage, Long> {

    Optional<MinigameStage> findByStageNo(Integer stageNo);

    List<MinigameStage> findAllByOrderByStageNoAsc();

    long countBy();
}
