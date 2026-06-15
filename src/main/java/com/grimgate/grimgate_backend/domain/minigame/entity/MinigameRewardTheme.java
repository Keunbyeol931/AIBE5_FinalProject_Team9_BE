package com.grimgate.grimgate_backend.domain.minigame.entity;

import com.grimgate.grimgate_backend.domain.theme.entity.Theme;
import com.grimgate.grimgate_backend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 미니게임 엔딩 화면 추천 테마 큐레이션.
 * - 비어 있으면 service 단에서 인기순(rating DESC) 폴백
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "minigame_reward_theme")
public class MinigameRewardTheme extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id", nullable = false)
    private Theme theme;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Builder
    public MinigameRewardTheme(Theme theme, Integer displayOrder, boolean active) {
        this.theme = theme;
        this.displayOrder = displayOrder;
        this.active = active;
    }
}
