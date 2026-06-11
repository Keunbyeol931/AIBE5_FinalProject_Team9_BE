package com.grimgate.grimgate_backend.domain.owner.controller;

import com.grimgate.grimgate_backend.domain.owner.dto.OwnerReservationSearchRequest;
import com.grimgate.grimgate_backend.domain.owner.dto.OwnerReservationResponse;
import com.grimgate.grimgate_backend.domain.owner.dto.ReviewReportHideRequest;
import com.grimgate.grimgate_backend.domain.owner.service.OwnerService;
import com.grimgate.grimgate_backend.domain.review.service.ReviewReportService;
import com.grimgate.grimgate_backend.domain.theme.dto.ThemeCreateRequest;
import com.grimgate.grimgate_backend.domain.theme.dto.ThemeResponse;
import com.grimgate.grimgate_backend.domain.theme.dto.ThemeUpdateRequest;
import com.grimgate.grimgate_backend.global.response.ApiResponse;
import com.grimgate.grimgate_backend.domain.owner.dto.OwnerReservationStatsResponse;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/owner")
public class OwnerController {

    private final OwnerService ownerService;
    private final ReviewReportService reviewReportService;

    @GetMapping("/themes")
    public ResponseEntity<List<ThemeResponse>> getOwnerThemes(){
        return ResponseEntity.ok(ownerService.getOwnerThemes());
    }

    @PostMapping("/themes")
    public ResponseEntity<Void> createTheme(
            @RequestBody @Valid ThemeCreateRequest request) {
        ownerService.createTheme(request);
        return ResponseEntity.ok().build();
    }

    //수정
    @PatchMapping("/themes/{themeId}")
    public ResponseEntity<Void> updateTheme(
            @PathVariable Long themeId,
            @RequestBody @Valid ThemeUpdateRequest request) {
        ownerService.updateTheme(themeId, request);
        return ResponseEntity.ok().build();
    }

    //삭제
    @DeleteMapping("/themes/{themeId}")
    public ResponseEntity<Void> deleteTheme(@PathVariable Long themeId) {
        ownerService.deleteTheme(themeId);
        return ResponseEntity.ok().build();
    }

    //예약 목록 검색
    @GetMapping("/reservations")
    public ResponseEntity<ApiResponse<Page<OwnerReservationResponse>>> searchReservations(
            @ModelAttribute OwnerReservationSearchRequest request,
            @PageableDefault(
                    sort = {"timeSlot.slotDate", "timeSlot.startTime"},
                    direction = Sort.Direction.ASC
            ) Pageable pageable
    ) {
        Page<OwnerReservationResponse> response = ownerService.searchReservations(request, pageable);
        return ResponseEntity.ok(ApiResponse.success("예약 목록 조회가 완료되었습니다.", response));
    }

    // 사장님 신고 복구 처리
    @PatchMapping("/review-reports/{reportId}/restore")
    public ResponseEntity<Void> restoreByOwner(@PathVariable Long reportId) {
        reviewReportService.restoreByOwner(reportId);
        return ResponseEntity.ok().build();
    }

    // 사장님 관리자 숨김 요청
    @PatchMapping("/review-reports/{reportId}/request-hide")
    public ResponseEntity<Void> requestHideByOwner(
            @PathVariable Long reportId,
            @RequestBody @Valid ReviewReportHideRequest request) {
        reviewReportService.requestHideByOwner(reportId, request);
        return ResponseEntity.ok().build();
    }

    // 예약 통계 조회
    @GetMapping("/reservations/stats")
    public ResponseEntity<ApiResponse<OwnerReservationStatsResponse>> getReservationStats(
            @RequestParam(value = "date_from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(value = "date_to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    ) {
        OwnerReservationStatsResponse response = ownerService.getReservationStats(dateFrom, dateTo);
        return ResponseEntity.ok(ApiResponse.success("예약 통계 조회가 완료되었습니다.", response));
    }
}

