package com.grimgate.grimgate_backend.domain.minigame.dto;

import com.grimgate.grimgate_backend.domain.minigame.entity.MinigamePlayStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record MinigameCompleteResponse(
        MinigamePlayStatus status,
        Integer durationSec,
        LocalDateTime completedAt,
        Long memberId
) {}
