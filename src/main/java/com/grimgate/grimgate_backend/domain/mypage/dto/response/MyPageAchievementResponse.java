package com.grimgate.grimgate_backend.domain.mypage.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MyPageAchievementResponse {

    private Long id;

    private String name;

    private String description;

    // 미획득이면 null
    private LocalDateTime acquiredAt;

    private boolean isAcquired;
}
