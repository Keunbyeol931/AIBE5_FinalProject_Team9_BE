package com.grimgate.grimgate_backend.domain.review.service;

import com.grimgate.grimgate_backend.domain.manager.entity.Manager;
import com.grimgate.grimgate_backend.domain.manager.repository.ManagerRepository;
import com.grimgate.grimgate_backend.domain.member.entity.Member;
import com.grimgate.grimgate_backend.domain.member.repository.MemberRepository;
import com.grimgate.grimgate_backend.domain.review.dto.ReviewReportCreateRequest;
import com.grimgate.grimgate_backend.domain.review.dto.ReviewReportHideRequest;
import com.grimgate.grimgate_backend.domain.review.dto.ReviewReportResponse;
import com.grimgate.grimgate_backend.domain.review.entity.Review;
import com.grimgate.grimgate_backend.domain.review.entity.ReviewReport;
import com.grimgate.grimgate_backend.domain.review.entity.ReviewReportStatus;
import com.grimgate.grimgate_backend.domain.review.repository.ReviewReportRepository;
import com.grimgate.grimgate_backend.domain.review.repository.ReviewRepository;
import com.grimgate.grimgate_backend.global.exception.CustomException;
import com.grimgate.grimgate_backend.global.exception.ErrorCode;
import com.grimgate.grimgate_backend.global.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 후기 신고/숨김 처리 서비스.
 *
 * <p>이번 PR 범위: RR-001(사용자 신고 접수) + RR-002/003(사장님 1차 판정) + 사장님 신고 목록 조회.
 * 관리자 단(RR-004~006)은 후속 PR.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewReportService {

    private final ReviewReportRepository reviewReportRepository;
    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;
    private final ManagerRepository managerRepository;

    // ============================================================
    // RR-001 사용자 신고 접수
    // ============================================================
    @Transactional
    public ReviewReportResponse reportReview(Long reviewId, ReviewReportCreateRequest request) {
        Long accountId = SecurityUtil.getCurrentAccountId();
        Member reporter = memberRepository.findByAccount_Id(accountId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

        // 본인이 작성한 후기는 신고 불가
        if (review.getMember().getId().equals(reporter.getId())) {
            throw new CustomException(ErrorCode.REVIEW_REPORT_SELF_FORBIDDEN);
        }

        // 중복 신고 차단 (DB 유니크 + 사전 체크)
        if (reviewReportRepository.existsByReview_IdAndReporter_Id(reviewId, reporter.getId())) {
            throw new CustomException(ErrorCode.REVIEW_REPORT_ALREADY_EXISTS);
        }

        ReviewReport saved = reviewReportRepository.save(
                ReviewReport.create(review, reporter, request.getReason(), request.getDetail())
        );
        return ReviewReportResponse.created(saved);
    }

    // ============================================================
    // 사장님 신고 목록 조회 (대시보드)
    // ============================================================
    public Page<ReviewReportResponse> getOwnerReports(ReviewReportStatus status, Pageable pageable) {
        Manager manager = getCurrentManager();
        return reviewReportRepository
                .findOwnerReports(manager.getId(), status, pageable)
                .map(ReviewReportResponse::forOwnerList);
    }

    // ============================================================
    // RR-002 사장님 복구 처리
    // ============================================================
    @Transactional
    public ReviewReportResponse restoreByOwner(Long reportId) {
        ReviewReport report = loadReportAsOwner(reportId);
        if (!report.isPendingOwnerReview()) {
            throw new CustomException(ErrorCode.REVIEW_REPORT_NOT_PENDING_OWNER);
        }

        Manager owner = getCurrentManager();
        report.restoreByOwner(owner);
        // 사장님 "문제없음" 판정 → 후기는 노출 유지 (이미 ACTIVE 이므로 명시적 변경 불필요)
        return ReviewReportResponse.ownerHandled(report);
    }

    // ============================================================
    // RR-003 사장님 관리자 숨김 요청
    // ============================================================
    @Transactional
    public ReviewReportResponse requestHideByOwner(Long reportId, ReviewReportHideRequest request) {
        ReviewReport report = loadReportAsOwner(reportId);
        if (!report.isPendingOwnerReview()) {
            throw new CustomException(ErrorCode.REVIEW_REPORT_NOT_PENDING_OWNER);
        }

        Manager owner = getCurrentManager();
        report.requestHideByOwner(owner, request.getOwnerReason());
        return ReviewReportResponse.ownerHandled(report);
    }

    // ============================================================
    // 공통 헬퍼
    // ============================================================

    /** 현재 로그인 사용자의 Manager 엔티티를 반환. (사장님 권한 검증) */
    private Manager getCurrentManager() {
        Long accountId = SecurityUtil.getCurrentAccountId();
        return managerRepository.findByAccount_Id(accountId)
                .orElseThrow(() -> new CustomException(ErrorCode.MANAGER_NOT_FOUND));
    }

    /** 현재 로그인 사용자(사장님)의 Member 엔티티. owner_id 컬럼 저장용. */
    private Member currentMember() {
        Long accountId = SecurityUtil.getCurrentAccountId();
        return memberRepository.findByAccount_Id(accountId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    /**
     * 신고를 사장님 권한으로 조회.
     * <p>본인이 매니저인 지점의 테마에 달린 후기 신고만 접근 가능. 아니면 NOT_FOUND/FORBIDDEN.
     */
    private ReviewReport loadReportAsOwner(Long reportId) {
        Manager manager = getCurrentManager();
        return reviewReportRepository.findByIdForOwner(reportId, manager.getId())
                .orElseThrow(() -> {
                    // 존재 자체가 없는지 / 권한이 없는지 구분
                    if (reviewReportRepository.existsById(reportId)) {
                        return new CustomException(ErrorCode.REVIEW_REPORT_ACCESS_DENIED);
                    }
                    return new CustomException(ErrorCode.REVIEW_REPORT_NOT_FOUND);
                });
    }
}
