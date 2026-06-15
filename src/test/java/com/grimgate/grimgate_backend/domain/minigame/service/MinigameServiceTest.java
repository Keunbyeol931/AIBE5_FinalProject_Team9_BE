package com.grimgate.grimgate_backend.domain.minigame.service;

import com.grimgate.grimgate_backend.domain.member.repository.MemberRepository;
import com.grimgate.grimgate_backend.domain.minigame.dto.*;
import com.grimgate.grimgate_backend.domain.minigame.entity.*;
import com.grimgate.grimgate_backend.domain.minigame.repository.*;
import com.grimgate.grimgate_backend.domain.theme.repository.ThemeRepository;
import com.grimgate.grimgate_backend.global.exception.CustomException;
import com.grimgate.grimgate_backend.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * MinigameService 단위 테스트.
 * - 정답 비교, 단계 순차 진행, 시도 횟수 제한, 타이머 만료 검증
 */
@ExtendWith(MockitoExtension.class)
class MinigameServiceTest {

    @Mock private MinigameStageRepository stageRepository;
    @Mock private MinigamePlayRepository playRepository;
    @Mock private MinigameStageAttemptRepository attemptRepository;
    @Mock private MinigameRewardThemeRepository rewardThemeRepository;
    @Mock private ThemeRepository themeRepository;
    @Mock private MemberRepository memberRepository;

    @InjectMocks
    private MinigameService minigameService;

    private MinigamePlay play;
    private MinigameStage stage1;
    private MinigameStage stage4;

    @BeforeEach
    void setUp() {
        play = MinigamePlay.start(null); // 비회원
        setField(play, "id", 100L);

        stage1 = MinigameStage.builder()
                .stageNo(1).stageType(MinigameStageType.CALENDAR_DEC)
                .answer("2025-12-30").build();
        stage4 = MinigameStage.builder()
                .stageNo(4).stageType(MinigameStageType.FINAL_CALENDAR)
                .answer("0702").build();
    }

    /* ========== start ========== */

    @Test
    @DisplayName("게임 시작 - 비회원도 플레이 토큰 발급 가능")
    void start_anonymous() {
        when(stageRepository.countBy()).thenReturn(4L);
        when(playRepository.save(any(MinigamePlay.class))).thenAnswer(i -> i.getArgument(0));

        MinigameStartResponse response = minigameService.start(null);

        assertThat(response.playToken()).isNotBlank();
        assertThat(response.currentStage()).isEqualTo(1);
        assertThat(response.totalStages()).isEqualTo(4);
        assertThat(response.timeLimitSec()).isEqualTo(420);
    }

    /* ========== getStage ========== */

    @Test
    @DisplayName("단계 정보 조회 - 정답(answer)은 응답에 포함되지 않는다")
    void getStage_doesNotExposeAnswer() {
        when(playRepository.findByPlayToken(play.getPlayToken())).thenReturn(Optional.of(play));
        when(stageRepository.findByStageNo(1)).thenReturn(Optional.of(stage1));

        MinigameStageResponse response = minigameService.getStage(play.getPlayToken(), 1);

        assertThat(response.stageNo()).isEqualTo(1);
        // DTO 자체에 answer 필드가 없으므로 코드 레벨에서 노출 불가 보장됨
        assertThat(response.stageType()).isEqualTo(MinigameStageType.CALENDAR_DEC);
    }

    @Test
    @DisplayName("이전 단계 미통과 시 다음 단계 조회 불가 (MINIGAME_STAGE_LOCKED)")
    void getStage_locked() {
        when(playRepository.findByPlayToken(play.getPlayToken())).thenReturn(Optional.of(play));

        // 현재 단계 1인데 2 조회 시도
        assertThatThrownBy(() -> minigameService.getStage(play.getPlayToken(), 2))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("message", ErrorCode.MINIGAME_STAGE_LOCKED.getMessage());
    }

    @Test
    @DisplayName("유효하지 않은 플레이 토큰이면 MINIGAME_PLAY_NOT_FOUND")
    void getStage_playNotFound() {
        when(playRepository.findByPlayToken("invalid")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> minigameService.getStage("invalid", 1))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("message", ErrorCode.MINIGAME_PLAY_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("타이머 만료 시 MINIGAME_TIME_EXPIRED")
    void getStage_expired() {
        setField(play, "startedAt", LocalDateTime.now().minusMinutes(10)); // 10분 전 시작 (7분 초과)
        when(playRepository.findByPlayToken(play.getPlayToken())).thenReturn(Optional.of(play));

        assertThatThrownBy(() -> minigameService.getStage(play.getPlayToken(), 1))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("message", ErrorCode.MINIGAME_TIME_EXPIRED.getMessage());
    }

    /* ========== verify ========== */

    @Test
    @DisplayName("정답 입력 시 다음 단계로 이동")
    void verify_correct_advancesStage() {
        when(playRepository.findByPlayToken(play.getPlayToken())).thenReturn(Optional.of(play));
        when(stageRepository.findByStageNo(1)).thenReturn(Optional.of(stage1));
        when(stageRepository.countBy()).thenReturn(4L);
        when(attemptRepository.countByPlayIdAndStageNo(100L, 1)).thenReturn(0L);

        MinigameVerifyResponse response = minigameService.verify(
                play.getPlayToken(), 1, new MinigameVerifyRequest("2025-12-30"));

        assertThat(response.correct()).isTrue();
        assertThat(response.nextStage()).isEqualTo(2);
        assertThat(response.isFinalStage()).isFalse();
        assertThat(play.getCurrentStage()).isEqualTo(2);
    }

    @Test
    @DisplayName("오답 시 단계는 그대로, 남은 시도 횟수 감소")
    void verify_wrong_keepsStage() {
        when(playRepository.findByPlayToken(play.getPlayToken())).thenReturn(Optional.of(play));
        when(stageRepository.findByStageNo(1)).thenReturn(Optional.of(stage1));
        when(stageRepository.countBy()).thenReturn(4L);
        when(attemptRepository.countByPlayIdAndStageNo(100L, 1)).thenReturn(2L);

        MinigameVerifyResponse response = minigameService.verify(
                play.getPlayToken(), 1, new MinigameVerifyRequest("wrong"));

        assertThat(response.correct()).isFalse();
        assertThat(response.nextStage()).isEqualTo(1);
        assertThat(response.attemptsLeft()).isEqualTo(2); // 5 - (2+1) = 2
        assertThat(play.getCurrentStage()).isEqualTo(1);
    }

    @Test
    @DisplayName("시도 횟수 5회 초과 시 MINIGAME_TOO_MANY_ATTEMPTS + 세션 FAILED")
    void verify_tooManyAttempts() {
        when(playRepository.findByPlayToken(play.getPlayToken())).thenReturn(Optional.of(play));
        when(attemptRepository.countByPlayIdAndStageNo(100L, 1)).thenReturn(5L);

        assertThatThrownBy(() -> minigameService.verify(
                play.getPlayToken(), 1, new MinigameVerifyRequest("anything")))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("message", ErrorCode.MINIGAME_TOO_MANY_ATTEMPTS.getMessage());

        assertThat(play.getStatus()).isEqualTo(MinigamePlayStatus.FAILED);
    }

    @Test
    @DisplayName("4단계 정답 시 CLEARED 상태로 전환 + isFinalStage true")
    void verify_finalStage_cleared() {
        setField(play, "currentStage", 4);
        when(playRepository.findByPlayToken(play.getPlayToken())).thenReturn(Optional.of(play));
        when(stageRepository.findByStageNo(4)).thenReturn(Optional.of(stage4));
        when(stageRepository.countBy()).thenReturn(4L);
        when(attemptRepository.countByPlayIdAndStageNo(100L, 4)).thenReturn(0L);

        MinigameVerifyResponse response = minigameService.verify(
                play.getPlayToken(), 4, new MinigameVerifyRequest("0702"));

        assertThat(response.correct()).isTrue();
        assertThat(response.isFinalStage()).isTrue();
        assertThat(play.getStatus()).isEqualTo(MinigamePlayStatus.CLEARED);
        assertThat(play.getCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("현재 단계가 아닌 다른 단계 verify 시도 시 MINIGAME_INVALID_STAGE")
    void verify_invalidStage() {
        when(playRepository.findByPlayToken(play.getPlayToken())).thenReturn(Optional.of(play));
        // currentStage = 1인데 2를 verify

        assertThatThrownBy(() -> minigameService.verify(
                play.getPlayToken(), 2, new MinigameVerifyRequest("anything")))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("message", ErrorCode.MINIGAME_INVALID_STAGE.getMessage());
    }

    @Test
    @DisplayName("정답은 대소문자/공백을 무시한다")
    void verify_caseAndWhitespaceInsensitive() {
        MinigameStage stage = MinigameStage.builder()
                .stageNo(3).stageType(MinigameStageType.EMPTY_SLOT)
                .answer("점심시간").build();
        setField(play, "currentStage", 3);
        when(playRepository.findByPlayToken(play.getPlayToken())).thenReturn(Optional.of(play));
        when(stageRepository.findByStageNo(3)).thenReturn(Optional.of(stage));
        when(stageRepository.countBy()).thenReturn(4L);
        when(attemptRepository.countByPlayIdAndStageNo(100L, 3)).thenReturn(0L);

        MinigameVerifyResponse response = minigameService.verify(
                play.getPlayToken(), 3, new MinigameVerifyRequest("  점심시간  "));

        assertThat(response.correct()).isTrue();
    }

    /* ========== complete ========== */

    @Test
    @DisplayName("complete - 만료 자동 처리")
    void complete_autoExpire() {
        setField(play, "startedAt", LocalDateTime.now().minusMinutes(10));
        when(playRepository.findByPlayToken(play.getPlayToken())).thenReturn(Optional.of(play));

        MinigameCompleteResponse response = minigameService.complete(play.getPlayToken());

        assertThat(response.status()).isEqualTo(MinigamePlayStatus.EXPIRED);
    }

    /* ========== ending ========== */

    @Test
    @DisplayName("엔딩 추천 - 큐레이션 비어있으면 인기순 폴백")
    void ending_fallback() {
        when(rewardThemeRepository.findByActiveTrueOrderByDisplayOrderAsc())
                .thenReturn(Collections.emptyList());
        when(themeRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        MinigameEndingResponse response = minigameService.getEndingRecommendations();

        assertThat(response.themes()).isEmpty();
    }

    /* ========== util ========== */

    private static void setField(Object target, String name, Object value) {
        try {
            Field f = findField(target.getClass(), name);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Field findField(Class<?> clazz, String name) throws NoSuchFieldException {
        Class<?> c = clazz;
        while (c != null) {
            try { return c.getDeclaredField(name); }
            catch (NoSuchFieldException e) { c = c.getSuperclass(); }
        }
        throw new NoSuchFieldException(name);
    }
}
