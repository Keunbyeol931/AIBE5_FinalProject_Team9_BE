package com.grimgate.grimgate_backend.domain.mate.dto;

import com.grimgate.grimgate_backend.domain.mate.entity.MateParticipant;
import com.grimgate.grimgate_backend.domain.mate.entity.MateParticipantStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 메이트 모집글 참여자 단건 응답 DTO.
 *
 * <p>응답 키는 카멜케이스, 엔티티 컬럼은 스네이크케이스라는 컨벤션을 따른다.</p>
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MateParticipantResponse {

    private Long id;
    private Long matePostId;
    private Long memberId;
    private String memberNickname;
    private MateParticipantStatus status;
    private LocalDateTime joinedAt;
    private LocalDateTime cancelledAt;

    public static MateParticipantResponse from(MateParticipant p) {
        return MateParticipantResponse.builder()
                .id(p.getId())
                .matePostId(p.getMatePost().getId())
                .memberId(p.getMember().getId())
                .memberNickname(p.getMember().getAccount().getNickname())
                .status(p.getStatus())
                .joinedAt(p.getJoinedAt())
                .cancelledAt(p.getCancelledAt())
                .build();
    }
}
