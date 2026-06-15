package com.grimgate.grimgate_backend.domain.minigame.service;

import com.grimgate.grimgate_backend.domain.member.entity.Member;
import com.grimgate.grimgate_backend.domain.member.repository.MemberRepository;
import com.grimgate.grimgate_backend.domain.minigame.dto.*;
import com.grimgate.grimgate_backend.domain.minigame.entity.*;
import com.grimgate.grimgate_backend.domain.minigame.repository.*;
import com.grimgate.grimgate_backend.domain.theme.entity.Theme;
import com.grimgate.grimgate_backend.domain.theme.repository.ThemeRepository;
import com.grimgate.grimgate_backend.global.exception.CustomException;
import com.grimgate.grimgate_backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * 미니게임 도메인 서비스.
 *
 * 핵심 정책:
 * - 정답(answer)은 서버 DB에만 보관, 응답에 절대 노출 X
 * - 단계 순차 진행 강제 (current_stage 검증)
 * - 타이머 서버 측 검증 (startedAt 기준)
 * - 단계별 시도 5회 제한
 * - 비회원도 플레이 가능 (member null 허용)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MinigameService {

    private static final int MAX_ATTEMPTS_PER_STAGE = 5;
    private static final int FALLBACK_RECOMMEND_COUNT = 3;

    private final MinigameStageRepository stageRepository;
    private final MinigamePlayRepository playRepository;
    private final MinigameStageAttemptRepository attemptRepository;
    private final MinigameRewardThemeRepository rewardThemeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    /* ============================================================
     * 1. 게임 시작 — 플레이 토큰 발급
     * ============================================================ */
    @Transactional
    public MinigameStartResponse start(Long currentAccountId) {
        Member member = resolveMember(currentAccountId);
        MinigamePlay play = MinigamePlay.start(member);
        playRepository.save(play);

        int totalStages = (int) stageRepository.countBy();

        return MinigameStartResponse.builder()
                .playToken(play.getPlayToken())
                .startedAt(play.getStartedAt())
                .timeLimitSec(play.getTimeLimitSec())
                .currentStage(play.getCurrentStage())
                .totalStages(totalStages)
                .build();
    }

    /* ============================================================
     * 2. 단계 정보 조회 (정답 제외)
     * ============================================================ */
    public MinigameStageResponse getStage(String playToken, Integer stageNo) {
        MinigamePlay play = loadPlay(playToken);
        ensureInProgressAndNotExpired(play);

        if (stageNo > play.getCurrentStage()) {
            throw new CustomException(ErrorCode.MINIGAME_STAGE_LOCKED);
        }

        MinigameStage stage = stageRepository.findByStageNo(stageNo)
                .orElseThrow(() -> new CustomException(ErrorCode.MINIGAME_INVALID_STAGE));

        return MinigameStageResponse.builder()
                .stageNo(stage.getStageNo())
                .stageType(stage.getStageType())
                .description(stage.getDescription())
                .hintText(stage.getHintText())
                .remainingSec(play.remainingSec(LocalDateTime.now()))
                .build();
    }

    /* ============================================================
     * 3. 정답 검증
     * ============================================================ */
    @Transactional
    public MinigameVerifyResponse verify(String playToken, Integer stageNo,
                                         MinigameVerifyRequest request) {
        MinigamePlay play = loadPlay(playToken);
        ensureInProgressAndNotExpired(play);

        // 현재 풀어야 할 단계인지 확인 (순차 강제)
        if (!play.getCurrentStage().equals(stageNo)) {
            throw new CustomException(ErrorCode.MINIGAME_INVALID_STAGE);
        }

        // 시도 횟수 확인
        long attempts = attemptRepository.countByPlayIdAndStageNo(play.getId(), stageNo);
        if (attempts >= MAX_ATTEMPTS_PER_STAGE) {
            play.markFailed();
            throw new CustomException(ErrorCode.MINIGAME_TOO_MANY_ATTEMPTS);
        }

        MinigameStage stage = stageRepository.findByStageNo(stageNo)
                .orElseThrow(() -> new CustomException(ErrorCode.MINIGAME_INVALID_STAGE));

        boolean correct = stage.matches(request.answer());
        attemptRepository.save(MinigameStageAttempt.builder()
                .play(play)
                .stageNo(stageNo)
                .submittedAnswer(request.answer())
                .correct(correct)
                .build());

        int totalStages = (int) stageRepository.countBy();
        boolean isFinalStage = false;
        Integer nextStage = play.getCurrentStage();

        if (correct) {
            isFinalStage = stageNo.equals(totalStages);
            play.advanceStage(totalStages);
            nextStage = play.getCurrentStage();
        }

        Integer attemptsLeft = (int) (MAX_ATTEMPTS_PER_STAGE - (attempts + 1));

        return MinigameVerifyResponse.builder()
                .correct(correct)
                .nextStage(nextStage)
                .isFinalStage(isFinalStage)
                .remainingSec(play.remainingSec(LocalDateTime.now()))
                .attemptsLeft(Math.max(attemptsLeft, 0))
                .build();
    }

    /* ============================================================
     * 4. 게임 완료 (상태 조회)
     * ============================================================ */
    @Transactional
    public MinigameCompleteResponse complete(String playToken) {
        MinigamePlay play = loadPlay(playToken);

        // 만료 자동 처리
        if (play.isInProgress() && play.isExpired(LocalDateTime.now())) {
            play.markExpired();
        }

        Long memberId = play.getMember() != null ? play.getMember().getId() : null;

        return MinigameCompleteResponse.builder()
                .status(play.getStatus())
                .durationSec(play.getDurationSec())
                .completedAt(play.getCompletedAt())
                .memberId(memberId)
                .build();
    }

    /* ============================================================
     * 5. 엔딩 추천 테마
     * ============================================================ */
    public MinigameEndingResponse getEndingRecommendations() {
        List<MinigameRewardTheme> curated =
                rewardThemeRepository.findByActiveTrueOrderByDisplayOrderAsc();

        List<MinigameEndingResponse.RecommendedTheme> themes;

        if (!curated.isEmpty()) {
            themes = curated.stream()
                    .map(rt -> toRecommendedTheme(rt.getTheme(), rt.getDisplayOrder()))
                    .toList();
        } else {
            // 폴백: rating 높은 순 (null은 뒤로), 동률이면 reviewCount 많은 순
            List<Theme> fallback = themeRepository.findAll(
                    PageRequest.of(0, FALLBACK_RECOMMEND_COUNT,
                            Sort.by(Sort.Order.desc("rating"), Sort.Order.desc("reviewCount")))
            ).getContent();

            // rating null인 데이터가 섞일 수 있어 메모리에서 보정
            themes = fallback.stream()
                    .sorted(Comparator.comparing((Theme t) -> t.getRating() == null ? Double.NEGATIVE_INFINITY : t.getRating())
                            .reversed())
                    .limit(FALLBACK_RECOMMEND_COUNT)
                    .map(t -> toRecommendedTheme(t, null))
                    .toList();
        }

        return MinigameEndingResponse.builder().themes(themes).build();
    }

    /* ============================================================
     * 6. 내 플레이 기록
     * ============================================================ */
    public MinigameMyPlayResponse getMyPlays(Long currentAccountId) {
        Member member = memberRepository.findByAccount_Id(currentAccountId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        List<MinigamePlay> plays = playRepository.findByMemberIdOrderByStartedAtDesc(member.getId());

        long cleared = playRepository.countByMemberIdAndStatus(member.getId(), MinigamePlayStatus.CLEARED);

        Integer best = plays.stream()
                .filter(p -> p.getStatus() == MinigamePlayStatus.CLEARED && p.getDurationSec() != null)
                .map(MinigamePlay::getDurationSec)
                .min(Integer::compareTo)
                .orElse(null);

        List<MinigameMyPlayResponse.PlayItem> items = plays.stream()
                .map(p -> MinigameMyPlayResponse.PlayItem.builder()
                        .playId(p.getId())
                        .status(p.getStatus())
                        .durationSec(p.getDurationSec())
                        .startedAt(p.getStartedAt())
                        .completedAt(p.getCompletedAt())
                        .build())
                .toList();

        return MinigameMyPlayResponse.builder()
                .plays(items)
                .totalCleared(cleared)
                .bestDurationSec(best)
                .build();
    }

    /* ============================================================
     * 내부 헬퍼
     * ============================================================ */
    private MinigamePlay loadPlay(String playToken) {
        if (playToken == null || playToken.isBlank()) {
            throw new CustomException(ErrorCode.MINIGAME_PLAY_NOT_FOUND);
        }
        return playRepository.findByPlayToken(playToken)
                .orElseThrow(() -> new CustomException(ErrorCode.MINIGAME_PLAY_NOT_FOUND));
    }

    private void ensureInProgressAndNotExpired(MinigamePlay play) {
        if (!play.isInProgress()) {
            throw new CustomException(ErrorCode.MINIGAME_ALREADY_FINISHED);
        }
        if (play.isExpired(LocalDateTime.now())) {
            play.markExpired();
            throw new CustomException(ErrorCode.MINIGAME_TIME_EXPIRED);
        }
    }

    private Member resolveMember(Long currentAccountId) {
        if (currentAccountId == null) return null;
        return memberRepository.findByAccount_Id(currentAccountId).orElse(null);
    }

    private MinigameEndingResponse.RecommendedTheme toRecommendedTheme(Theme theme, Integer order) {
        return MinigameEndingResponse.RecommendedTheme.builder()
                .themeId(theme.getId())
                .title(theme.getTitle())
                .branchName(theme.getBranch() != null ? theme.getBranch().getBranchName() : null)
                .thumbnailUrl(theme.getThumbnailUrl())
                .difficulty(theme.getDifficulty())
                .rating(theme.getRating())
                .displayOrder(order)
                .build();
    }
}
