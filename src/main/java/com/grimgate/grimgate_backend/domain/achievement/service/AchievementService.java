package com.grimgate.grimgate_backend.domain.achievement.service;

import com.grimgate.grimgate_backend.domain.achievement.entity.Achievement;
import com.grimgate.grimgate_backend.domain.achievement.entity.MemberAchievement;
import com.grimgate.grimgate_backend.domain.achievement.repository.AchievementRepository;
import com.grimgate.grimgate_backend.domain.achievement.repository.MemberAchievementRepository;
import com.grimgate.grimgate_backend.domain.member.entity.Member;
import com.grimgate.grimgate_backend.domain.member.repository.MemberRepository;
import com.grimgate.grimgate_backend.domain.mypage.dto.response.MyPageAchievementResponse;
import com.grimgate.grimgate_backend.global.exception.CustomException;
import com.grimgate.grimgate_backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 업적 관련 비즈니스 로직을 처리하는 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AchievementService {

    private final MemberRepository memberRepository;
    private final AchievementRepository achievementRepository;
    private final MemberAchievementRepository memberAchievementRepository;

    /**
     * 업적 목록 조회 (전체 업적 기준, 획득 여부 포함)
     */
    public List<MyPageAchievementResponse> getAchievements(Long accountId) {
        Member member = memberRepository.findByAccount_Id(accountId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        List<Achievement> allAchievements = achievementRepository.findAll();
        List<MemberAchievement> memberAchievements = memberAchievementRepository.findByMember_Id(member.getId());

        // 획득한 업적 ID → MemberAchievement 맵
        Map<Long, MemberAchievement> acquiredMap = memberAchievements.stream()
                .collect(Collectors.toMap(ma -> ma.getAchievement().getId(), ma -> ma));

        return allAchievements.stream()
                .map(achievement -> {
                    MemberAchievement ma = acquiredMap.get(achievement.getId());
                    return MyPageAchievementResponse.builder()
                            .id(achievement.getId())
                            .name(achievement.getName())
                            .description(achievement.getDescription())
                            .isAcquired(ma != null)
                            .acquiredAt(ma != null ? ma.getAcquiredAt() : null)
                            .build();
                })
                .collect(Collectors.toList());
    }
}
