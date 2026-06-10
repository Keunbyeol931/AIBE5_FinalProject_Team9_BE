package com.grimgate.grimgate_backend.domain.mypage.controller;

import com.grimgate.grimgate_backend.domain.achievement.service.AchievementService;
import com.grimgate.grimgate_backend.domain.mate.dto.MateParticipantResponse;
import com.grimgate.grimgate_backend.domain.mate.service.MateParticipantService;
import com.grimgate.grimgate_backend.domain.mypage.dto.request.MyPageProfileUpdateRequest;
import com.grimgate.grimgate_backend.domain.mypage.dto.response.MyPageAchievementResponse;
import com.grimgate.grimgate_backend.domain.mypage.dto.response.MyPageMainResponse;
import com.grimgate.grimgate_backend.domain.mypage.dto.response.MyPageMatePostResponse;
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
    private final MateParticipantService mateParticipantService;

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
    // 후기 생성
    @PostMapping("/reviews")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @Valid @RequestBody ReviewCreateRequest request) {
        ReviewResponse response = mypageReservationService.createReview(request);
        return ResponseEntity.ok(ApiResponse.success("후기 생성 성공", response));
    }

    // 내 메이트 참여 목록 조회
    @GetMapping("/mypage/mate-participations")
    public ResponseEntity<ApiResponse<List<MateParticipantResponse>>> getMyMateParticipations() {
        Long accountId = SecurityUtil.getCurrentAccountId();
        List<MateParticipantResponse> response = mateParticipantService.myParticipations(accountId);
        return ResponseEntity.ok(ApiResponse.success("내 메이트 참여 목록 조회 성공", response));
    }

    // 내 메이트 모집글 조회
    @GetMapping("/mypage/mate-posts")
    public ResponseEntity<ApiResponse<List<MyPageMatePostResponse>>> getMyMatePosts() {
        List<MyPageMatePostResponse> response = mypageActivityService.getMyMatePosts();
        return ResponseEntity.ok(ApiResponse.success("내 메이트 모집글 조회 성공", response));
    }

    // 내 후기 조회
    @GetMapping("/reviews")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getMyReviews() {
        List<ReviewResponse> response = mypageActivityService.getMyReviews();
        return ResponseEntity.ok(ApiResponse.success("내 후기 조회 성공", response));
    }

    // 내 후기 수정
    @PatchMapping("/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateMyReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewUpdateRequest request) {
        ReviewResponse response = mypageActivityService.updateMyReview(reviewId, request);
        return ResponseEntity.ok(ApiResponse.success("후기 수정 성공", response));
    }

    // 내 후기 삭제
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteMyReview(@PathVariable Long reviewId) {
        mypageActivityService.deleteMyReview(reviewId);
        return ResponseEntity.ok().build();
    }
}
