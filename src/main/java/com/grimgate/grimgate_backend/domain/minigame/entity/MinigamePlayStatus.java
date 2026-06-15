package com.grimgate.grimgate_backend.domain.minigame.entity;

/**
 * 미니게임 플레이 세션 상태.
 */
public enum MinigamePlayStatus {
    IN_PROGRESS,  // 진행 중
    CLEARED,      // 클리어 완료
    FAILED,       // 실패 (시도 횟수 초과 등)
    EXPIRED       // 시간 초과
}
