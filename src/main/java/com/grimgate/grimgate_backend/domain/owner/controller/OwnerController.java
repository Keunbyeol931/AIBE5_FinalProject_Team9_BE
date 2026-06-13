package com.grimgate.grimgate_backend.domain.owner.controller;

import com.grimgate.grimgate_backend.domain.owner.dto.OwnerReservationSearchRequest;
import com.grimgate.grimgate_backend.domain.owner.dto.OwnerReservationResponse;
import com.grimgate.grimgate_backend.domain.owner.service.OwnerService;
import com.grimgate.grimgate_backend.domain.theme.dto.*;
import com.grimgate.grimgate_backend.global.response.ApiResponse;
import com.grimgate.grimgate_backend.domain.owner.dto.OwnerReservationStatsResponse;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestPart;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/owner")
public class OwnerController {

    private final OwnerService ownerService;

    @GetMapping("/themes")
    public ResponseEntity<List<ThemeResponse>> getOwnerThemes(){
        return ResponseEntity.ok(ownerService.getOwnerThemes());
    }

    @PostMapping(value = "/themes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "테마 등록",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            encoding = {@Encoding(name = "request", contentType = "application/json")}
                    )
            )
    )
    public ResponseEntity<ThemeCreateResponse> createTheme(
            @RequestPart(value = "request") @Valid ThemeCreateRequest request,
            @RequestPart(value = "thumbnail") MultipartFile thumbnail) {
        ThemeCreateResponse response = ownerService.createTheme(request, thumbnail);
        return ResponseEntity.ok(response);
    }

    //수정
    @PatchMapping(value = "/themes/{themeId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "테마 수정",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            encoding = {@Encoding(name = "request", contentType = "application/json")}
                    )
            )
    )
    public ResponseEntity<ThemeUpdateResponse> updateTheme(
            @PathVariable Long themeId,
            @RequestPart(value = "request") @Valid ThemeUpdateRequest request,
            @RequestPart(value = "thumbnail",required = false) MultipartFile thumbnail) {
        ThemeUpdateResponse response = ownerService.updateTheme(themeId, request, thumbnail);
        return ResponseEntity.ok(response);
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

    // 예약 통계 조회
    @GetMapping("/reservations/stats")
    public ResponseEntity<ApiResponse<OwnerReservationStatsResponse>> getReservationStats(
            @RequestParam(value = "dateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(value = "dateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    ) {
        OwnerReservationStatsResponse response = ownerService.getReservationStats(dateFrom, dateTo);
        return ResponseEntity.ok(ApiResponse.success("예약 통계 조회가 완료되었습니다.", response));
    }
}

