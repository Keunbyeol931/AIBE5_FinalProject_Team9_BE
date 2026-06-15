package com.grimgate.grimgate_backend.domain.minigame.entity;

import com.grimgate.grimgate_backend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 미니게임 단계 메타데이터 + 정답 보관.
 * - 정답(answer)은 절대 API 응답에 포함되어서는 안 된다.
 * - 시드 데이터로 단계 1~4 미리 등록.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "minigame_stage",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_minigame_stage_no",
                columnNames = "stage_no"
        )
)
public class MinigameStage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stage_no", nullable = false)
    private Integer stageNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "stage_type", nullable = false, length = 30)
    private MinigameStageType stageType;

    @Column(name = "answer", nullable = false, length = 100)
    private String answer;

    @Column(name = "hint_text", columnDefinition = "TEXT")
    private String hintText;

    @Column(name = "description", length = 255)
    private String description;

    @Builder
    public MinigameStage(Integer stageNo, MinigameStageType stageType,
                         String answer, String hintText, String description) {
        this.stageNo = stageNo;
        this.stageType = stageType;
        this.answer = answer;
        this.hintText = hintText;
        this.description = description;
    }

    /**
     * 정답 비교 (대소문자 무시, 공백 제거).
     */
    public boolean matches(String submitted) {
        if (submitted == null) return false;
        return this.answer.trim().equalsIgnoreCase(submitted.trim());
    }
}
