package com.grimgate.grimgate_backend.domain.review.service;

import com.grimgate.grimgate_backend.domain.manager.entity.Manager;
import com.grimgate.grimgate_backend.domain.manager.repository.ManagerRepository;
import com.grimgate.grimgate_backend.domain.member.entity.Member;
import com.grimgate.grimgate_backend.domain.member.repository.MemberRepository;
import com.grimgate.grimgate_backend.domain.review.dto.ReviewReportCreateRequest;
import com.grimgate.grimgate_backend.domain.owner.dto.ReviewReportHideRequest;
import com.grimgate.grimgate_backend.domain.review.entity.Review;
import com.grimgate.grimgate_backend.domain.review.entity.ReviewReport;
import com.grimgate.grimgate_backend.domain.review.repository.ReviewReportRepository;
import com.grimgate.grimgate_backend.domain.review.repository.ReviewRepository;
import com.grimgate.grimgate_backend.global.exception.CustomException;
import com.grimgate.grimgate_backend.global.exception.ErrorCode;
import com.grimgate.grimgate_backend.global.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 후기 신고 서비스
@Service
@RequiredArgsConstructor
@Transactional
public class ReviewReportService {

    private final ReviewReportRepository reviewReportRepository;
    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;
    private final ManagerRepository managerRepository;

    // 후기 신고 접수
    public void createReport(Long reviewId, ReviewReportCreateRequest request) {
        // 1. 로그인 사용자 accountId 추출
        Long accountId = SecurityUtil.getCurrentAccountId();

        // 2. Member 조회
        Member reporter = memberRepository.findByAccount_Id(accountId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 3. Review 조회
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

        // 4. 동일 유저 중복 신고 체크
        if (reviewReportRepository.existsByReview_IdAndReporter_Id(reviewId, reporter.getId())) {
            throw new CustomException(ErrorCode.REVIEW_REPORT_ALREADY_EXISTS);
        }

        // 5. 신고 엔티티 생성
        ReviewReport report = ReviewReport.create(review, reporter, request.getReason(), request.getDetail());

        // 6. 저장
        reviewReportRepository.save(report);
    }

    // 사장님 후기 복구 처리
    public void restoreByOwner(Long reportId) {
        // 1. 로그인 사용자 accountId 추출
        Long accountId = SecurityUtil.getCurrentAccountId();

        // 2. Manager 조회
        Manager manager = managerRepository.findByAccount_Id(accountId)
                .orElseThrow(() -> new CustomException(ErrorCode.MANAGER_NOT_FOUND));

        // 3. 신고 내역 조회
        ReviewReport report = reviewReportRepository.findById(reportId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_REPORT_NOT_FOUND));

        // 4. 복구 처리 (review.status는 변경하지 않음)
        report.restoreByOwner(manager);
    }

    // 사장님 관리자 검토 요청 (숨김 처리)
    public void requestHideByOwner(Long reportId, ReviewReportHideRequest request) {
        // 1. 로그인 사용자 accountId 추출
        Long accountId = SecurityUtil.getCurrentAccountId();

        // 2. Manager 조회
        Manager manager = managerRepository.findByAccount_Id(accountId)
                .orElseThrow(() -> new CustomException(ErrorCode.MANAGER_NOT_FOUND));

        // 3. 신고 내역 조회
        ReviewReport report = reviewReportRepository.findById(reportId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_REPORT_NOT_FOUND));

        // 4. 관리자 검토 요청 처리 (review.status는 관리자 승인 전까지 ACTIVE 유지)
        report.requestHideByOwner(manager, request.getOwnerReason());
    }
}
