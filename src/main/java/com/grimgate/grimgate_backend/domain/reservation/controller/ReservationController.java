package com.grimgate.grimgate_backend.domain.reservation.controller;

import com.grimgate.grimgate_backend.domain.reservation.dto.ReservationCreateRequest;
import com.grimgate.grimgate_backend.domain.reservation.dto.ReservationCreateResponse;
import com.grimgate.grimgate_backend.domain.reservation.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 예약 관련 API를 제공하는 Controller 클래스입니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    /**
     * 예약을 생성하고 PENDING_PAYMENT 상태로 저장합니다.
     *
     * @param request 예약 생성 요청 정보 DTO
     * @return 예약 생성 결과 정보 DTO
     */
    @PostMapping
    public ResponseEntity<ReservationCreateResponse> createReservation(
            @Valid @RequestBody ReservationCreateRequest request
    ) {
        ReservationCreateResponse response = reservationService.createReservation(request);
        return ResponseEntity.ok(response);
    }
}
