package com.grimgate.grimgate_backend.domain.minigame.controller;

import com.grimgate.grimgate_backend.domain.minigame.dto.*;
import com.grimgate.grimgate_backend.domain.minigame.service.MinigameService;
import com.grimgate.grimgate_backend.global.security.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 미니게임 API.
 *
 * 인증 정책:
 * - /start, /stages, /verify, /complete, /ending : 비회원 허용
 * - /me/plays : 로그인 필수
 *
 * 세션 식별: X-Minigame-Play-Token 헤더 (UUID)
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/minigame")
public class MinigameController {

    private static final String PLAY_TOKEN_HEADER = "X-Minigame-Play-Token";

    private final MinigameService minigameService;

    /** 1. 게임 시작 — playToken 발급 */
    @PostMapping("/start")
    public ResponseEntity<MinigameStartResponse> start() {
        Long accountId = getOptionalAccountId();
        MinigameStartResponse response = minigameService.start(accountId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** 2. 단계 정보 조회 (정답 제외) */
    @GetMapping("/stages/{stageNo}")
    public ResponseEntity<MinigameStageResponse> getStage(
            @RequestHeader(PLAY_TOKEN_HEADER) String playToken,
            @PathVariable Integer stageNo
    ) {
        return ResponseEntity.ok(minigameService.getStage(playToken, stageNo));
    }

    /** 3. 정답 검증 */
    @PostMapping("/stages/{stageNo}/verify")
    public ResponseEntity<MinigameVerifyResponse> verify(
            @RequestHeader(PLAY_TOKEN_HEADER) String playToken,
            @PathVariable Integer stageNo,
            @Valid @RequestBody MinigameVerifyRequest request
    ) {
        return ResponseEntity.ok(minigameService.verify(playToken, stageNo, request));
    }

    /** 4. 게임 완료 (상태 조회/확정) */
    @PostMapping("/complete")
    public ResponseEntity<MinigameCompleteResponse> complete(
            @RequestHeader(PLAY_TOKEN_HEADER) String playToken
    ) {
        return ResponseEntity.ok(minigameService.complete(playToken));
    }

    /** 5. 엔딩 추천 테마 */
    @GetMapping("/ending/recommendations")
    public ResponseEntity<MinigameEndingResponse> getEndingRecommendations() {
        return ResponseEntity.ok(minigameService.getEndingRecommendations());
    }

    /** 6. 내 플레이 기록 (로그인 필수) */
    @GetMapping("/me/plays")
    public ResponseEntity<MinigameMyPlayResponse> getMyPlays() {
        Long accountId = SecurityUtil.getCurrentAccountId();
        return ResponseEntity.ok(minigameService.getMyPlays(accountId));
    }

    /** 비회원 허용 엔드포인트용 */
    private Long getOptionalAccountId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(String.valueOf(auth.getPrincipal()))) {
            return null;
        }
        try {
            return SecurityUtil.getCurrentAccountId();
        } catch (Exception e) {
            return null;
        }
    }
}
