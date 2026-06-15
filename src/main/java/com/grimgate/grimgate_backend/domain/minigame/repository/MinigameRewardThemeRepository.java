package com.grimgate.grimgate_backend.domain.minigame.repository;

import com.grimgate.grimgate_backend.domain.minigame.entity.MinigameRewardTheme;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MinigameRewardThemeRepository extends JpaRepository<MinigameRewardTheme, Long> {

    List<MinigameRewardTheme> findByActiveTrueOrderByDisplayOrderAsc();
}
