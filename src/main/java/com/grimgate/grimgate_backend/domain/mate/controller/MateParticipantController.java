package com.grimgate.grimgate_backend.domain.mate.controller;

import com.grimgate.grimgate_backend.domain.mate.dto.MateParticipantListResponse;
import com.grimgate.grimgate_backend.domain.mate.dto.MateParticipantResponse;
import com.grimgate.grimgate_backend.domain.mate.service.MateParticipantService;
import com.grimgate.grimgate_backend.global.security.SecurityUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 메이트 모집글 참여 컨트롤러.
 *
 * <p>base path: {@code /api/mate-posts/{postId}/participants}</p>
 *
 * <ul>
 *   <li>POST   /api/mate-posts/{postId}/participants                 — 참여 신청 (로그인 필수)</li>
 *   <li>DELETE /api/mate-posts/{postId}/participants/me              — 본인 참여 취소</li>
 *   <li>DELETE /api/mate-posts/{postId}/participants/{memberId}      — 강퇴 (작성자만)</li>
 *   <li>GET    /api/mate-posts/{postId}/participants                 — 참여자 목록 (비로그인 허용)</li>
 *   <li>GET    /api/mate-posts/me/participations                     — 내 참여 목록 (로그인 필수)</li>
 * </ul>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mate-posts")
public class MateParticipantController {

    private final MateParticipantService mateParticipantService;

    /** 참여 신청 */
    @PostMapping("/{postId}/participants")
    public ResponseEntity<MateParticipantResponse> join(@PathVariable Long postId) {
        Long accountId = SecurityUtil.getCurrentAccountId();
        MateParticipantResponse res = mateParticipantService.join(accountId, postId);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    /** 본인 참여 취소 */
    @DeleteMapping("/{postId}/participants/me")
    public ResponseEntity<Void> cancel(@PathVariable Long postId) {
        Long accountId = SecurityUtil.getCurrentAccountId();
        mateParticipantService.cancel(accountId, postId);
        return ResponseEntity.noContent().build();
    }

    /** 작성자가 참여자 강퇴 */
    @DeleteMapping("/{postId}/participants/{memberId}")
    public ResponseEntity<Void> kick(@PathVariable Long postId,
                                     @PathVariable Long memberId) {
        Long accountId = SecurityUtil.getCurrentAccountId();
        mateParticipantService.kick(accountId, postId, memberId);
        return ResponseEntity.noContent().build();
    }

    /** 참여자 목록 조회 */
    @GetMapping("/{postId}/participants")
    public ResponseEntity<MateParticipantListResponse> list(@PathVariable Long postId) {
        return ResponseEntity.ok(mateParticipantService.listParticipants(postId));
    }

    /** 내 참여 목록 */
    @GetMapping("/me/participations")
    public ResponseEntity<List<MateParticipantResponse>> myParticipations() {
        Long accountId = SecurityUtil.getCurrentAccountId();
        return ResponseEntity.ok(mateParticipantService.myParticipations(accountId));
    }
}
