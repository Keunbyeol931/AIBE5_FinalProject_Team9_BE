package com.grimgate.grimgate_backend.domain.minigame.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record MinigameStartResponse(
        String playToken,
        LocalDateTime startedAt,
        Integer timeLimitSec,
        Integer currentStage,
        Integer totalStages
) {}
