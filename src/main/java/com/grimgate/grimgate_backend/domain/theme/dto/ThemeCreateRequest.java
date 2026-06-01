package com.grimgate.grimgate_backend.domain.theme.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ThemeCreateRequest {
    //테마명, 난이도,공포도,최대인원, 연령제한, 장르,테마 설명, 대표이미지

    private String title;
    @Min(1) @Max(5)
    private Integer difficulty;

    @Min(1) @Max(5)
    private Integer horrorLevel;
    @Min(1) @Max(6)
    private Integer minPeople;
    @Min(1) @Max(6)
    private Integer maxPeople;
    private Integer ageLimit;
    private Integer playTime;
    private String tags;
    private Integer price;
    private String description;
    private String thumbnailUrl;

}
