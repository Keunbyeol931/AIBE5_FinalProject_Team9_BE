package com.grimgate.grimgate_backend.domain.mypage.controller;

import com.grimgate.grimgate_backend.domain.achievement.service.AchievementService;
import com.grimgate.grimgate_backend.domain.mypage.dto.request.MyPageProfileUpdateRequest;
import com.grimgate.grimgate_backend.domain.mypage.dto.response.MyPageAchievementResponse;
import com.grimgate.grimgate_backend.domain.mypage.dto.response.MyPageMainResponse;
import com.grimgate.grimgate_backend.domain.mypage.dto.response.MyPageProfileResponse;
import com.grimgate.grimgate_backend.domain.mypage.dto.response.MyPageReservationResponse;
import com.grimgate.grimgate_backend.domain.mypage.dto.response.MyPageStatsResponse;
import com.grimgate.grimgate_backend.domain.mypage.facade.MyPageFacade;
import com.grimgate.grimgate_backend.domain.mypage.service.MyPageActivityService;
import com.grimgate.grimgate_backend.domain.mypage.service.MyPageReservationService;
import com.grimgate.grimgate_backend.domain.mypage.service.MyPageService;
import com.grimgate.grimgate_backend.domain.review.dto.ReviewCreateRequest;
import com.grimgate.grimgate_backend.domain.review.dto.ReviewResponse;
import com.grimgate.grimgate_backend.domain.review.dto.ReviewUpdateRequest;
import com.grimgate.grimgate_backend.global.response.ApiResponse;
import com.grimgate.grimgate_backend.global.security.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageFacade myPageFacade;
    private final MyPageService myPageService;
    private final AchievementService achievementService;
    private final MyPageReservationService mypageReservationService;
    private final MyPageActivityService mypageActivityService;

    // 마이페이지 메인 조회 (프로필 + 통계)
    @GetMapping("/mypage")
    public ResponseEntity<ApiResponse<MyPageMainResponse>> getMyPageMain() {
        Long accountId = SecurityUtil.getCurrentAccountId();
        MyPageMainResponse response = myPageFacade.getMyPageMain(accountId);
        return ResponseEntity.ok(ApiResponse.success("마이페이지 조회 성공", response));
    }

    // 프로필 조회
    @GetMapping("/mypage/profile")
    public ResponseEntity<ApiResponse<MyPageProfileResponse>> getProfile() {
        Long accountId = SecurityUtil.getCurrentAccountId();
        MyPageProfileResponse response = myPageService.getProfile(accountId);
        return ResponseEntity.ok(ApiResponse.success("프로필 조회 성공", response));
    }

    // 통계 조회
    @GetMapping("/mypage/stats")
    public ResponseEntity<ApiResponse<MyPageStatsResponse>> getStats() {
        Long accountId = SecurityUtil.getCurrentAccountId();
        MyPageStatsResponse response = myPageService.getStats(accountId);
        return ResponseEntity.ok(ApiResponse.success("통계 조회 성공", response));
    }

    // 예약 목록 조회 (UPCOMING / PAST)
    @GetMapping("/mypage/reservations")
    public ResponseEntity<ApiResponse<List<MyPageReservationResponse>>> getReservations(
            @RequestParam(defaultValue = "UPCOMING") String type) {
        Long accountId = SecurityUtil.getCurrentAccountId();
        List<MyPageReservationResponse> response = myPageService.getReservations(accountId, type);
        return ResponseEntity.ok(ApiResponse.success("예약 목록 조회 성공", response));
    }

    // 업적 목록 조회
    @GetMapping("/mypage/achievements")
    public ResponseEntity<ApiResponse<List<MyPageAchievementResponse>>> getAchievements() {
        Long accountId = SecurityUtil.getCurrentAccountId();
        List<MyPageAchievementResponse> response = achievementService.getAchievements(accountId);
        return ResponseEntity.ok(ApiResponse.success("업적 목록 조회 성공", response));
    }

    // 프로필 수정
    @PatchMapping("/mypage/profile")
    public ResponseEntity<ApiResponse<Void>> updateProfile(
            @RequestBody MyPageProfileUpdateRequest request) {
        Long accountId = SecurityUtil.getCurrentAccountId();
        myPageService.updateProfile(accountId, request);
        return ResponseEntity.ok(ApiResponse.success("프로필 수정 성공", null));
    }
    // TODO: ApiResponse 래퍼 적용 필요 - 현재 날것으로 반환 중
    // 후기 생성
    @PostMapping("/reviews")
    public ResponseEntity<ReviewResponse> createReview(
            @Valid @RequestBody ReviewCreateRequest request) {
        return ResponseEntity.ok(mypageReservationService.createReview(request));
    }

    // 내 후기 조회
    @GetMapping("/reviews")
    public ResponseEntity<List<ReviewResponse>> getMyReviews() {
        return ResponseEntity.ok(mypageActivityService.getMyReviews());
    }

    // 내 후기 수정
    @PatchMapping("/reviews/{reviewId}")
    public ResponseEntity<ReviewResponse> updateMyReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewUpdateRequest request) {
        return ResponseEntity.ok(mypageActivityService.updateMyReview(reviewId, request));
    }

    // 내 후기 삭제
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteMyReview(@PathVariable Long reviewId) {
        mypageActivityService.deleteMyReview(reviewId);
        return ResponseEntity.ok().build();
    }
}
