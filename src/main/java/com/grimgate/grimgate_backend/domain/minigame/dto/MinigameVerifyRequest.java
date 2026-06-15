package com.grimgate.grimgate_backend.domain.minigame.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MinigameVerifyRequest(
        @NotBlank(message = "정답을 입력해주세요.")
        @Size(max = 100, message = "정답은 100자를 초과할 수 없습니다.")
        String answer
) {}
