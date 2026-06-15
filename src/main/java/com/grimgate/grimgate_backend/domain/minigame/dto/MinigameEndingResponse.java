package com.grimgate.grimgate_backend.domain.minigame.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record MinigameEndingResponse(
        List<RecommendedTheme> themes
) {
    @Builder
    public record RecommendedTheme(
            Long themeId,
            String title,
            String branchName,
            String thumbnailUrl,
            Integer difficulty,
            Double rating,
            Integer displayOrder
    ) {}
}
