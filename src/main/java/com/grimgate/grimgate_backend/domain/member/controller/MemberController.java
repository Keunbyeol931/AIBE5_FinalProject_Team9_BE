package com.grimgate.grimgate_backend.domain.member.controller;

import com.grimgate.grimgate_backend.domain.mypage.dto.response.MyPageProfileResponse;
import com.grimgate.grimgate_backend.domain.mypage.dto.response.MyPageStatsResponse;
import com.grimgate.grimgate_backend.domain.mypage.service.MyPageService;
import com.grimgate.grimgate_backend.global.response.ApiResponse;
import com.grimgate.grimgate_backend.global.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members/me")
@RequiredArgsConstructor
public class MemberController {

    private final MyPageService myPageService;

    // MB-001 프로필 조회
    @GetMapping("")
    public ResponseEntity<MyPageProfileResponse> getProfile() {
        Long accountId = SecurityUtil.getCurrentAccountId();
        MyPageProfileResponse response = myPageService.getProfile(accountId);
        return ResponseEntity.ok(response);
    }

    // MB-005 통계 조회
    @GetMapping("/stats")
    public ResponseEntity<MyPageStatsResponse> getStats() {
        Long accountId = SecurityUtil.getCurrentAccountId();
        MyPageStatsResponse response = myPageService.getStats(accountId);
        return ResponseEntity.ok(response);
    }

    // 예약 목록 조회
    @GetMapping("/reservations")
    public ResponseEntity<ApiResponse<?>> getReservations() {
        Long accountId = SecurityUtil.getCurrentAccountId();
        // TODO: 예약 목록 조회 구현
        return null;
    }

    // 리뷰 목록 조회
    @GetMapping("/reviews")
    public ResponseEntity<ApiResponse<?>> getReviews() {
        Long accountId = SecurityUtil.getCurrentAccountId();
        // TODO: 리뷰 목록 조회 구현
        return null;
    }

    // 메이트 모집글 목록 조회
    @GetMapping("/mate-posts")
    public ResponseEntity<ApiResponse<?>> getMatePosts() {
        Long accountId = SecurityUtil.getCurrentAccountId();
        // TODO: 메이트 모집글 목록 조회 구현
        return null;
    }

    // 메이트 참여 목록 조회
    @GetMapping("/mate-participations")
    public ResponseEntity<ApiResponse<?>> getMateParticipations() {
        Long accountId = SecurityUtil.getCurrentAccountId();
        // TODO: 메이트 참여 목록 조회 구현
        return null;
    }

    // 프로필 수정
    @PatchMapping("")
    public ResponseEntity<ApiResponse<?>> updateProfile() {
        Long accountId = SecurityUtil.getCurrentAccountId();
        // TODO: 프로필 수정 구현
        return null;
    }

    // 비밀번호 수정
    @PatchMapping("/password")
    public ResponseEntity<ApiResponse<?>> updatePassword() {
        Long accountId = SecurityUtil.getCurrentAccountId();
        // TODO: 비밀번호 수정 구현
        return null;
    }
}
