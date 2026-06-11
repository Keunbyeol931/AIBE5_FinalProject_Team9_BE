package com.grimgate.grimgate_backend.domain.review.entity;

import com.grimgate.grimgate_backend.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 후기 신고 엔티티.
 *
 * <p>한 회원이 한 후기에 대해 1건만 신고할 수 있다(unique 제약).
 * 신고 → 사장님 1차 판정 → (필요 시) 관리자 2차 판정으로 흐른다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "review_report",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_review_report_review_reporter",
                columnNames = {"review_id", "reporter_id"}
        )
)
public class ReviewReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 신고 대상 후기 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    /** 신고자 (회원) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private Member reporter;

    /** 신고 사유 코드/카테고리 (자유 문자열) */
    @Column(nullable = false, length = 50)
    private String reason;

    /** 신고 상세 사유 (선택) */
    @Column(columnDefinition = "TEXT")
    private String detail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReviewReportStatus status;

    /** 사장님 처리자 (RR-002/003 시) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private Member owner;

    /** 사장님 숨김요청 사유 */
    @Column(name = "owner_reason", columnDefinition = "TEXT")
    private String ownerReason;

    @Column(name = "owner_handled_at")
    private LocalDateTime ownerHandledAt;

    /** 관리자 처리자 (RR-005/006 시, 이번 PR에서는 미사용) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private Member admin;

    @Column(name = "admin_reason", columnDefinition = "TEXT")
    private String adminReason;

    @Column(name = "admin_handled_at")
    private LocalDateTime adminHandledAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ---------- 정적 생성 ----------

    public static ReviewReport create(Review review, Member reporter, String reason, String detail) {
        ReviewReport r = new ReviewReport();
        r.review = review;
        r.reporter = reporter;
        r.reason = reason;
        r.detail = detail;
        r.status = ReviewReportStatus.PENDING_OWNER_REVIEW;
        return r;
    }

    // ---------- 도메인 메서드 ----------

    /** 사장님 "문제없음" 복구 처리 */
    public void restoreByOwner(Member owner) {
        this.owner = owner;
        this.status = ReviewReportStatus.OWNER_RESTORED;
        this.ownerHandledAt = LocalDateTime.now();
        this.resolvedAt = this.ownerHandledAt;
    }

    /** 사장님 관리자 숨김 요청 */
    public void requestHideByOwner(Member owner, String ownerReason) {
        this.owner = owner;
        this.ownerReason = ownerReason;
        this.status = ReviewReportStatus.REQUESTED_ADMIN_REVIEW;
        this.ownerHandledAt = LocalDateTime.now();
    }

    /** 사장님이 1차 판정을 완료한 상태인지 */
    public boolean isPendingOwnerReview() {
        return this.status == ReviewReportStatus.PENDING_OWNER_REVIEW;
    }
}
