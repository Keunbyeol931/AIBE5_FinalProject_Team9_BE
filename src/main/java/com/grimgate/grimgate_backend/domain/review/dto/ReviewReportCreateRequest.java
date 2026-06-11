package com.grimgate.grimgate_backend.domain.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** RR-001 사용자 신고 접수 요청. */
@Getter
@NoArgsConstructor
public class ReviewReportCreateRequest {

    @NotBlank(message = "신고 사유는 필수입니다.")
    @Size(max = 50)
    private String reason;

    @Size(max = 1000)
    private String detail;
}
