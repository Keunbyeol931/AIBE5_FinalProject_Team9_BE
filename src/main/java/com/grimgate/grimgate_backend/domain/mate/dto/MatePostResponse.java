package com.grimgate.grimgate_backend.domain.mate.dto;

import com.grimgate.grimgate_backend.domain.mate.entity.ExperienceLevel;
import com.grimgate.grimgate_backend.domain.mate.entity.MatePost;
import com.grimgate.grimgate_backend.domain.mate.entity.MatePostStatus;
import com.grimgate.grimgate_backend.domain.member.entity.Member;
import com.grimgate.grimgate_backend.domain.theme.entity.Theme;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 메이트 모집글 상세 응답 DTO.
 * — openChatUrl 은 참가자/작성자에게만 제공 (서비스 레이어에서 결정)
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MatePostResponse {

    private Long id;
    private Long memberId;
    private String authorNickname;
    private Long themeId;
    private String themeTitle;
    private String title;
    private String content;
    private String imageUrl;
    private LocalDateTime meetingTime;
    private LocalDateTime deadline;
    private Integer currentPeople;
    private Integer maxPeople;
    private List<String> tags;
    private ExperienceLevel experienceLevel;
    /** 참가자/작성자에게만 노출, 비참가자에겐 null */
    private String openChatUrl;
    private MatePostStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static MatePostResponse of(MatePost post, boolean canSeeOpenChat) {
        Member member = post.getMember();
        Theme theme = post.getTheme();
        return MatePostResponse.builder()
                .id(post.getId())
                .memberId(member != null ? member.getId() : null)
                .authorNickname(resolveNickname(member))
                .themeId(theme != null ? theme.getId() : null)
                .themeTitle(theme != null ? theme.getTitle() : null)
                .title(post.getTitle())
                .content(post.getContent())
                .imageUrl(post.getImageUrl())
                .meetingTime(post.getMeetingTime())
                .deadline(post.getDeadline())
                .currentPeople(post.getCurrentPeople())
                .maxPeople(post.getMaxPeople())
                .tags(parseTags(post.getTags()))
                .experienceLevel(post.getExperienceLevel())
                .openChatUrl(canSeeOpenChat ? post.getOpenChatUrl() : null)
                .status(post.getStatus())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    private static List<String> parseTags(String raw) {
        if (raw == null || raw.isBlank()) return Collections.emptyList();
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    /** Account.nickname 을 작성자 닉네임으로 표시 */
    private static String resolveNickname(Member member) {
        if (member == null || member.getAccount() == null) return null;
        return member.getAccount().getNickname();
    }
}
