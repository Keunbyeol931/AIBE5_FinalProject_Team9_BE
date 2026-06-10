package com.grimgate.grimgate_backend.domain.member.controller;

import com.grimgate.grimgate_backend.global.response.ApiResponse;
import com.grimgate.grimgate_backend.global.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members/me")
@RequiredArgsConstructor
public class MemberController {

    // 메이트 모집글 목록 조회
    @GetMapping("/mate-posts")
    public ResponseEntity<ApiResponse<?>> getMatePosts() {
        Long accountId = SecurityUtil.getCurrentAccountId();
        // TODO: 메이트 모집글 목록 조회 구현
        return null;
    }



}
