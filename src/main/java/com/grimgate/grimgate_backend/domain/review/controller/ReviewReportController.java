package com.grimgate.grimgate_backend.domain.review.controller;

import com.grimgate.grimgate_backend.domain.review.dto.ReviewReportCreateRequest;
import com.grimgate.grimgate_backend.domain.review.dto.ReviewReportResponse;
import com.grimgate.grimgate_backend.domain.review.service.ReviewReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * RR-001 사용자 신고 접수 컨트롤러.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewReportController {

    private final ReviewReportService reviewReportService;

    /** RR-001 POST /api/reviews/{id}/reports — 후기 신고 접수. */
    @PostMapping("/{id}/reports")
    public ResponseEntity<ReviewReportResponse> reportReview(
            @PathVariable("id") Long reviewId,
            @RequestBody @Valid ReviewReportCreateRequest request
    ) {
        ReviewReportResponse body = reviewReportService.reportReview(reviewId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }
}
