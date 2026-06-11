package com.grimgate.grimgate_backend.domain.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 관리자 후기 신고 승인/반려 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminReviewDecisionRequest {

    @NotBlank(message = "처리 사유는 필수입니다.")
    private String adminReason;
}
