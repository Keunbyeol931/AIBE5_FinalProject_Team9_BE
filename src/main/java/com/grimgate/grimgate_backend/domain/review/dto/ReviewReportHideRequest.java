package com.grimgate.grimgate_backend.domain.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** RR-003 사장님 관리자 숨김 요청. */
@Getter
@NoArgsConstructor
public class ReviewReportHideRequest {

    @NotBlank(message = "숨김 요청 사유는 필수입니다.")
    @Size(max = 1000)
    private String ownerReason;
}
