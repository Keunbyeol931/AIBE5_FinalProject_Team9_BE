package com.grimgate.grimgate_backend.domain.mypage.service;

import com.grimgate.grimgate_backend.domain.achievement.repository.AchievementRepository;
import com.grimgate.grimgate_backend.domain.achievement.repository.MemberAchievementRepository;
import com.grimgate.grimgate_backend.domain.member.entity.Member;
import com.grimgate.grimgate_backend.domain.member.repository.MemberRepository;
import com.grimgate.grimgate_backend.domain.member.service.TitleCalculator;
import com.grimgate.grimgate_backend.domain.mypage.dto.response.MyPageProfileResponse;
import com.grimgate.grimgate_backend.domain.mypage.dto.response.MyPageStatsResponse;
import com.grimgate.grimgate_backend.domain.reservation.entity.Reservation;
import com.grimgate.grimgate_backend.domain.reservation.repository.ReservationRepository;
import com.grimgate.grimgate_backend.domain.title.repository.TitleRepository;
import com.grimgate.grimgate_backend.global.exception.CustomException;
import com.grimgate.grimgate_backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 마이페이지 관련 비즈니스 로직을 처리하는 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;
    private final TitleCalculator titleCalculator;
    private final MemberAchievementRepository memberAchievementRepository;
    private final AchievementRepository achievementRepository;
    private final TitleRepository titleRepository;

    /**
     * 마이페이지 통계 정보 조회 및 칭호 갱신
     */
    @Transactional
    public MyPageStatsResponse getStats(Long accountId) {
        Member member = memberRepository.findByAccount_Id(accountId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        List<Reservation> reservations = reservationRepository.findByMemberWithTimeSlot(member);

        long totalPlayCount = titleCalculator.calcTotalPlayCount(reservations);
        long clearedCount = titleCalculator.calcClearedCount(reservations);
        double successRate = titleCalculator.calcSuccessRate(totalPlayCount, clearedCount);

        // 조건에 맞는 칭호 id가 있으면 업데이트
        Optional<Long> matchingTitleId = titleCalculator.findMatchingTitleId((int) totalPlayCount, successRate);
        matchingTitleId.ifPresent(member::updateTitleId);

        long acquiredAchievementCount = memberAchievementRepository.countByMember_Id(member.getId());
        long totalAchievementCount = achievementRepository.count();

        return MyPageStatsResponse.builder()
                .totalPlayCount((int) totalPlayCount)
                .successRate((int) successRate)
                .bestClearTime(null)
                .acquiredAchievementCount(acquiredAchievementCount)
                .totalAchievementCount(totalAchievementCount)
                .build();
    }

    /**
     * 마이페이지 프로필 정보 조회
     */
    public MyPageProfileResponse getProfile(Long accountId) {
        Member member = memberRepository.findByAccount_Id(accountId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 칭호명 조회 (없으면 null)
        String titleName = null;
        if (member.getTitleId() != null) {
            titleName = titleRepository.findById(member.getTitleId())
                    .map(title -> title.getName())
                    .orElse(null);
        }

        // 성별 공개 여부 확인
        String gender = member.getAccount().isGenderVisible() ? member.getAccount().getGender() : null;

        // 나이 공개 여부 확인
        Integer age = member.getAccount().isAgeVisible() ? member.getAccount().getAge() : null;

        return MyPageProfileResponse.builder()
                .nickname(member.getAccount().getNickname())
                .titleName(titleName)
                .gender(gender)
                .age(age)
                .profileCharacterImageUrl(member.getProfileCharacter().getImageUrl())
                .build();
    }
}
