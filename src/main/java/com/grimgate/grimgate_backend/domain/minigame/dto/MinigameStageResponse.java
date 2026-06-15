package com.grimgate.grimgate_backend.domain.minigame.dto;

import com.grimgate.grimgate_backend.domain.minigame.entity.MinigameStageType;
import lombok.Builder;

/**
 * 단계 정보 응답 — 정답(answer)은 절대 포함하지 않는다.
 */
@Builder
public record MinigameStageResponse(
        Integer stageNo,
        MinigameStageType stageType,
        String description,
        String hintText,
        Long remainingSec
) {}
