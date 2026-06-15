package com.grimgate.grimgate_backend.domain.minigame.entity;

/**
 * 미니게임 단계 타입.
 * 기획서 기준 4단계 퍼즐.
 */
public enum MinigameStageType {
    CALENDAR_DEC,    // 1단계: 12월 달력
    ENTRY_LOG,       // 2단계: 출입 기록
    EMPTY_SLOT,      // 3단계: 비어있는 시간대
    FINAL_CALENDAR   // 4단계: 최종 달력
}
