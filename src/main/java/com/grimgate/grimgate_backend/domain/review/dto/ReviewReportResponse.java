package com.grimgate.grimgate_backend.domain.review.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.grimgate.grimgate_backend.domain.review.entity.ReviewReport;
import com.grimgate.grimgate_backend.domain.review.entity.ReviewReportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/** RR-001 ~ RR-003 공통 응답 DTO. 사용처에 맞춰 필드를 선택적으로 채운다. */
@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReviewReportResponse {

    private Long id;
    private Long reviewId;
    private Long reporterId;
    private ReviewReportStatus status;
    private LocalDateTime createdAt;

    // 신고 사유 / 상세 (대시보드 노출용)
    private String reason;
    private String detail;

    // 사장님 처리 결과 (RR-002 / RR-003 응답에 포함). owner = Manager 엔티티 PK.
    private Long ownerId;
    private String ownerReason;
    private LocalDateTime ownerHandledAt;

    // 사장님 신고 목록(대시보드)에서 노출용 부가 정보
    private String reviewContent;
    private String reviewStatus;
    private String themeTitle;
    private String branchName;

    /** RR-001 응답: 접수 직후 최소 필드. */
    public static ReviewReportResponse created(ReviewReport r) {
        return ReviewReportResponse.builder()
                .id(r.getId())
                .reviewId(r.getReview().getId())
                .reporterId(r.getReporter().getId())
                .status(r.getStatus())
                .createdAt(r.getCreatedAt())
                .build();
    }

    /** RR-002 / RR-003 응답: 사장님 처리 결과. */
    public static ReviewReportResponse ownerHandled(ReviewReport r) {
        return ReviewReportResponse.builder()
                .id(r.getId())
                .reviewId(r.getReview().getId())
                .status(r.getStatus())
                .ownerId(r.getOwner() != null ? r.getOwner().getId() : null)
                .ownerReason(r.getOwnerReason())
                .ownerHandledAt(r.getOwnerHandledAt())
                .build();
    }

    /** 사장님 신고 목록(대시보드) 응답: 후기/테마 정보까지 노출. */
    public static ReviewReportResponse forOwnerList(ReviewReport r) {
        return ReviewReportResponse.builder()
                .id(r.getId())
                .reviewId(r.getReview().getId())
                .reporterId(r.getReporter().getId())
                .status(r.getStatus())
                .createdAt(r.getCreatedAt())
                .reason(r.getReason())
                .detail(r.getDetail())
                .ownerHandledAt(r.getOwnerHandledAt())
                .ownerReason(r.getOwnerReason())
                .reviewContent(r.getReview().getContent())
                .reviewStatus(r.getReview().getStatus())
                .themeTitle(r.getReview().getTheme().getTitle())
                .branchName(r.getReview().getTheme().getBranch().getBranchName())
                .build();
    }
}
