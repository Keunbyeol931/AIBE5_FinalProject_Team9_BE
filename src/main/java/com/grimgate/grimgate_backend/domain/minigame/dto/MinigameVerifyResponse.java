package com.grimgate.grimgate_backend.domain.minigame.dto;

import lombok.Builder;

@Builder
public record MinigameVerifyResponse(
        boolean correct,
        Integer nextStage,
        boolean isFinalStage,
        Long remainingSec,
        Integer attemptsLeft
) {}
