package com.grimgate.grimgate_backend.domain.theme.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ThemeUpdateRequest {
    private String title;
    private String description;
    private String tags;
    private Integer horrorLevel;
    private Integer difficulty;
    private Integer ageLimit;
    private Integer playTime;
    private Integer minPeople;
    private Integer maxPeople;
    private Integer price;
    private String thumbnailUrl;
}