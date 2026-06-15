package com.grimgate.grimgate_backend.domain.minigame.repository;

import com.grimgate.grimgate_backend.domain.minigame.entity.MinigamePlay;
import com.grimgate.grimgate_backend.domain.minigame.entity.MinigamePlayStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MinigamePlayRepository extends JpaRepository<MinigamePlay, Long> {

    Optional<MinigamePlay> findByPlayToken(String playToken);

    List<MinigamePlay> findByMemberIdOrderByStartedAtDesc(Long memberId);

    long countByMemberIdAndStatus(Long memberId, MinigamePlayStatus status);
}
