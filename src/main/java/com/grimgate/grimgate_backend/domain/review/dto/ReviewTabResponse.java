package com.grimgate.grimgate_backend.domain.review.dto;

import com.grimgate.grimgate_backend.global.response.TabCommonResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class ReviewTabResponse extends TabCommonResponse {
    private double averageRating;  // theme.getRating()
    private List<ReviewResponse> reviews;  // 기존 ReviewResponse 재사용
    private Map<Integer, Integer> ratingDistribution; // 별점 분포도


    public ReviewTabResponse(Double rating, Integer reviewCount, Integer playTime,
                             Integer minPeople, Integer maxPeople, String thumbnailUrl,
                             double averageRating,
                             Map<Integer, Integer> ratingDistribution, List<ReviewResponse> reviews) {
        super(rating, reviewCount, playTime, minPeople, maxPeople, thumbnailUrl);
        this.averageRating = averageRating;
        this.ratingDistribution = ratingDistribution;
        this.reviews = reviews;
    }
}
