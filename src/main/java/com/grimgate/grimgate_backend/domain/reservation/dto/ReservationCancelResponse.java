package com.grimgate.grimgate_backend.domain.reservation.dto;

import lombok.Builder;
import lombok.Getter;

// 예약 취소 처리 결과를 응답하기 위한 DTO 클래스입니다.
@Getter
@Builder
public class ReservationCancelResponse {

    private final Long reservationId;
    private final String status;

    public ReservationCancelResponse(Long reservationId, String status) {
        this.reservationId = reservationId;
        this.status = status;
    }
}
