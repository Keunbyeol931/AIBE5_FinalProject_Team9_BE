package com.grimgate.grimgate_backend.domain.achievement.repository;

import com.grimgate.grimgate_backend.domain.achievement.entity.MemberAchievement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberAchievementRepository extends JpaRepository<MemberAchievement, Long> {

    long countByMember_Id(Long memberId);

    List<MemberAchievement> findByMember_Id(Long memberId);
}
