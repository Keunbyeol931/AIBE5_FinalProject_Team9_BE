package com.grimgate.grimgate_backend.domain.owner.controller;

import com.grimgate.grimgate_backend.domain.review.dto.ReviewReportHideRequest;
import com.grimgate.grimgate_backend.domain.review.dto.ReviewReportResponse;
import com.grimgate.grimgate_backend.domain.review.entity.ReviewReportStatus;
import com.grimgate.grimgate_backend.domain.review.service.ReviewReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 사장님(Owner) 후기 신고 처리 컨트롤러.
 *
 * <p>- GET    /api/owner/review-reports               : 사장님 대시보드용 신고 목록 (신규)
 * <p>- PATCH  /api/owner/review-reports/{id}/restore       : RR-002 신고 복구
 * <p>- PATCH  /api/owner/review-reports/{id}/request-hide  : RR-003 관리자 숨김 요청
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/owner/review-reports")
public class OwnerReviewReportController {

    private final ReviewReportService reviewReportService;

    /** 사장님 대시보드: 본인 지점 테마에 달린 후기 신고 목록. status 로 필터링 가능. */
    @GetMapping
    public ResponseEntity<Page<ReviewReportResponse>> list(
            @RequestParam(value = "status", required = false) ReviewReportStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(reviewReportService.getOwnerReports(status, pageable));
    }

    /** RR-002 사장님 복구. */
    @PatchMapping("/{id}/restore")
    public ResponseEntity<ReviewReportResponse> restore(@PathVariable("id") Long reportId) {
        return ResponseEntity.ok(reviewReportService.restoreByOwner(reportId));
    }

    /** RR-003 사장님 관리자 숨김 요청. */
    @PatchMapping("/{id}/request-hide")
    public ResponseEntity<ReviewReportResponse> requestHide(
            @PathVariable("id") Long reportId,
            @RequestBody @Valid ReviewReportHideRequest request
    ) {
        return ResponseEntity.ok(reviewReportService.requestHideByOwner(reportId, request));
    }
}
