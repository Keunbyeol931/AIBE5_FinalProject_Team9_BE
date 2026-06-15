package com.grimgate.grimgate_backend.domain.minigame.dto;

import com.grimgate.grimgate_backend.domain.minigame.entity.MinigamePlayStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record MinigameMyPlayResponse(
        List<PlayItem> plays,
        Long totalCleared,
        Integer bestDurationSec
) {
    @Builder
    public record PlayItem(
            Long playId,
            MinigamePlayStatus status,
            Integer durationSec,
            LocalDateTime startedAt,
            LocalDateTime completedAt
    ) {}
}
