package com.grimgate.grimgate_backend.domain.admin.service;

import com.grimgate.grimgate_backend.domain.account.entity.Account;
import com.grimgate.grimgate_backend.domain.account.repository.AccountRepository;
import com.grimgate.grimgate_backend.domain.admin.dto.request.AdminReviewDecisionRequest;
import com.grimgate.grimgate_backend.domain.admin.dto.request.AdminReviewSearchRequest;
import com.grimgate.grimgate_backend.domain.admin.dto.response.AdminReviewDetailResponse;
import com.grimgate.grimgate_backend.domain.admin.dto.response.AdminReviewResponse;
import com.grimgate.grimgate_backend.domain.admin.dto.response.AdminReviewReportResponse;
import com.grimgate.grimgate_backend.domain.admin.dto.response.AdminReviewStatsResponse;
import com.grimgate.grimgate_backend.domain.review.entity.Review;
import com.grimgate.grimgate_backend.domain.review.entity.ReviewImage;
import com.grimgate.grimgate_backend.domain.review.entity.ReviewReport;
import com.grimgate.grimgate_backend.domain.review.entity.ReviewReportStatus;
import com.grimgate.grimgate_backend.domain.review.repository.ReviewImageRepository;
import com.grimgate.grimgate_backend.domain.review.repository.ReviewReportRepository;
import com.grimgate.grimgate_backend.domain.review.repository.ReviewRepository;
import com.grimgate.grimgate_backend.domain.review.repository.ReviewSpecification;
import com.grimgate.grimgate_backend.global.exception.CustomException;
import com.grimgate.grimgate_backend.global.exception.ErrorCode;
import com.grimgate.grimgate_backend.global.security.SecurityUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 관리자 후기 신고 처리 서비스
@Service
@RequiredArgsConstructor
@Transactional
public class AdminReviewService {

    private final ReviewReportRepository reviewReportRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final AccountRepository accountRepository;

    // 관리자 후기 통계 카드 조회
    @Transactional(readOnly = true)
    public AdminReviewStatsResponse getStats() {
        long total   = reviewRepository.count();
        long active  = reviewRepository.countByStatus("ACTIVE");
        long hidden  = reviewRepository.countByStatus("HIDDEN");
        long pending = reviewReportRepository.countByStatus(ReviewReportStatus.REQUESTED_ADMIN_REVIEW);
        return AdminReviewStatsResponse.of(total, active, hidden, pending);
    }

    // 관리자 후기 목록 조회 (검색/필터 조건 기반)
    @Transactional(readOnly = true)
    public Page<AdminReviewResponse> getReviews(AdminReviewSearchRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getLimit());

        Specification<Review> spec = Specification
                .where(ReviewSpecification.statusEquals(request.getStatus()))
                .and(ReviewSpecification.themeIdEquals(request.getThemeId()))
                .and(ReviewSpecification.createdAtBetween(request.getDateFrom(), request.getDateTo()))
                .and(ReviewSpecification.keywordContains(request.getKeyword()));

        return reviewRepository.findAll(spec, pageable)
                .map(AdminReviewResponse::from);
    }

    // 관리자 후기 상세 조회
    @Transactional(readOnly = true)
    public AdminReviewDetailResponse getReviewDetail(Long reviewId) {
        // 1. 후기 조회
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

        // 2. 후기 이미지 목록 조회
        List<ReviewImage> images = reviewImageRepository.findByReview_Id(reviewId);

        // 3. 응답 DTO 반환
        return AdminReviewDetailResponse.from(review, images);
    }

    // 관리자 검토 요청된 신고 목록 조회 (REQUESTED_ADMIN_REVIEW 상태)
    @Transactional(readOnly = true)
    public Page<AdminReviewReportResponse> getReviewReports(int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<ReviewReport> reports = reviewReportRepository.findByStatus(
                ReviewReportStatus.REQUESTED_ADMIN_REVIEW, pageable
        );
        return reports.map(AdminReviewReportResponse::from);
    }

    // 관리자 후기 신고 승인 처리
    public void approveReport(Long reportId, AdminReviewDecisionRequest request) {
        // 1. 로그인 사용자 accountId 추출
        Long accountId = SecurityUtil.getCurrentAccountId();

        // 2. Account 조회
        Account admin = accountRepository.findByIdAndDeletedAtIsNull(accountId)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        // 3. 신고 내역 조회
        ReviewReport report = reviewReportRepository.findById(reportId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_REPORT_NOT_FOUND));

        // 4. 신고 승인 처리 (ReviewReport 상태 변경)
        report.approveByAdmin(admin, request.getAdminReason());

        // 5. 후기 숨김 처리
        report.getReview().hide();
    }

    // 관리자 후기 신고 반려 처리
    public void rejectReport(Long reportId, AdminReviewDecisionRequest request) {
        // 1. 로그인 사용자 accountId 추출
        Long accountId = SecurityUtil.getCurrentAccountId();

        // 2. Account 조회
        Account admin = accountRepository.findByIdAndDeletedAtIsNull(accountId)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        // 3. 신고 내역 조회
        ReviewReport report = reviewReportRepository.findById(reportId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_REPORT_NOT_FOUND));

        // 4. 신고 반려 처리 (ReviewReport 상태 변경)
        report.rejectByAdmin(admin, request.getAdminReason());

        // 5. 후기 원상 복구 처리
        report.getReview().restore();
    }
}
