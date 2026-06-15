package com.grimgate.grimgate_backend.domain.review.entity;

/**
 * 후기 신고 처리 상태.
 *
 * <p>흐름:
 * <pre>
 * PENDING_OWNER_REVIEW (신고 접수, 사장님 1차 검토 대기)
 *   ├── OWNER_RESTORED          : 사장님이 "문제없음" 판정 (후기 유지)
 *   └── REQUESTED_ADMIN_REVIEW  : 사장님이 관리자에게 숨김 요청
 *         ├── ADMIN_APPROVED    : 관리자 승인 (후기 숨김)
 *         └── ADMIN_REJECTED    : 관리자 거절 (후기 유지)
 * </pre>
 */
public enum ReviewReportStatus {

    /** 오너 검토 대기 중 */
    PENDING_OWNER_REVIEW,

    /** 오너가 후기 복구 처리 */
    OWNER_RESTORED,

    /** 관리자 검토 요청됨 */
    REQUESTED_ADMIN_REVIEW,

    /** 관리자 승인 (신고 인정 → 후기 숨김) */
    ADMIN_APPROVED,

    /** 관리자 거부 (신고 기각 → 후기 유지) */
    ADMIN_REJECTED
}
