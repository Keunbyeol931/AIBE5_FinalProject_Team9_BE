package com.grimgate.grimgate_backend.domain.minigame.entity;

import com.grimgate.grimgate_backend.domain.member.entity.Member;
import com.grimgate.grimgate_backend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 미니게임 플레이 세션.
 * - 비회원도 플레이 가능 (member 컬럼 NULL 허용)
 * - playToken(UUID)을 헤더로 받아 세션 식별
 * - 타이머는 startedAt + timeLimitSec 으로 서버에서 검증
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "minigame_play",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_minigame_play_token",
                columnNames = "play_token"
        ),
        indexes = {
                @Index(name = "idx_minigame_play_member_completed",
                        columnList = "member_id,completed_at")
        }
)
public class MinigamePlay extends BaseTimeEntity {

    public static final int DEFAULT_TIME_LIMIT_SEC = 420; // 7분

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 비회원 허용 → nullable */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "play_token", nullable = false, length = 36)
    private String playToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MinigamePlayStatus status;

    @Column(name = "current_stage", nullable = false)
    private Integer currentStage;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "duration_sec")
    private Integer durationSec;

    @Column(name = "time_limit_sec", nullable = false)
    private Integer timeLimitSec;

    @Builder
    private MinigamePlay(Member member, String playToken, MinigamePlayStatus status,
                         Integer currentStage, LocalDateTime startedAt,
                         Integer timeLimitSec) {
        this.member = member;
        this.playToken = playToken;
        this.status = status;
        this.currentStage = currentStage;
        this.startedAt = startedAt;
        this.timeLimitSec = timeLimitSec;
    }

    /**
     * 새 플레이 세션 생성 (비회원이면 member = null).
     */
    public static MinigamePlay start(Member member) {
        return MinigamePlay.builder()
                .member(member)
                .playToken(UUID.randomUUID().toString())
                .status(MinigamePlayStatus.IN_PROGRESS)
                .currentStage(1)
                .startedAt(LocalDateTime.now())
                .timeLimitSec(DEFAULT_TIME_LIMIT_SEC)
                .build();
    }

    /** 남은 시간(초). 음수면 만료. */
    public long remainingSec(LocalDateTime now) {
        long elapsed = Duration.between(startedAt, now).getSeconds();
        return timeLimitSec - elapsed;
    }

    public boolean isExpired(LocalDateTime now) {
        return remainingSec(now) <= 0;
    }

    public boolean isInProgress() {
        return status == MinigamePlayStatus.IN_PROGRESS;
    }

    /** 정답 맞춤 → 다음 단계로 이동. 마지막 단계면 CLEARED 처리. */
    public void advanceStage(int totalStages) {
        if (this.currentStage >= totalStages) {
            this.status = MinigamePlayStatus.CLEARED;
            this.completedAt = LocalDateTime.now();
            this.durationSec = (int) Duration.between(startedAt, completedAt).getSeconds();
        } else {
            this.currentStage += 1;
        }
    }

    public void markExpired() {
        if (this.status == MinigamePlayStatus.IN_PROGRESS) {
            this.status = MinigamePlayStatus.EXPIRED;
            this.completedAt = LocalDateTime.now();
            this.durationSec = (int) Duration.between(startedAt, completedAt).getSeconds();
        }
    }

    public void markFailed() {
        if (this.status == MinigamePlayStatus.IN_PROGRESS) {
            this.status = MinigamePlayStatus.FAILED;
            this.completedAt = LocalDateTime.now();
            this.durationSec = (int) Duration.between(startedAt, completedAt).getSeconds();
        }
    }
}
