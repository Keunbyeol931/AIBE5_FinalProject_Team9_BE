package com.grimgate.grimgate_backend.domain.minigame.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 단계별 정답 시도 로그.
 * 단계별 5회 초과 시 차단(어뷰징 방지).
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "minigame_stage_attempt",
        indexes = {
                @Index(name = "idx_attempt_play_stage", columnList = "play_id,stage_no")
        }
)
public class MinigameStageAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "play_id", nullable = false)
    private MinigamePlay play;

    @Column(name = "stage_no", nullable = false)
    private Integer stageNo;

    @Column(name = "submitted_answer", nullable = false, length = 100)
    private String submittedAnswer;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    @Column(name = "attempted_at", nullable = false)
    private LocalDateTime attemptedAt;

    @Builder
    public MinigameStageAttempt(MinigamePlay play, Integer stageNo,
                                String submittedAnswer, boolean correct) {
        this.play = play;
        this.stageNo = stageNo;
        this.submittedAnswer = submittedAnswer;
        this.correct = correct;
        this.attemptedAt = LocalDateTime.now();
    }
}
