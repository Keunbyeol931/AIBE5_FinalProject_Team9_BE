package com.grimgate.grimgate_backend.domain.payment.controller;

import com.grimgate.grimgate_backend.domain.payment.dto.PaymentReadyRequest;
import com.grimgate.grimgate_backend.domain.payment.dto.PaymentReadyResponse;
import com.grimgate.grimgate_backend.domain.payment.service.PaymentService;
import com.grimgate.grimgate_backend.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 결제 준비 API를 호출하여 결제 데이터를 생성하고 초기 상태를 설정합니다.
     *
     * @param request 결제 준비 요청 정보 DTO
     * @return 결제 준비 결과 정보 DTO (Toss widget 렌더링에 필요한 정보 포함)
     */
    @PostMapping("/ready")
    public ResponseEntity<ApiResponse<PaymentReadyResponse>> readyPayment(
            @Valid @RequestBody PaymentReadyRequest request
    ) {
        PaymentReadyResponse response = paymentService.readyPayment(request);
        return ResponseEntity.ok(ApiResponse.success("결제 준비가 완료되었습니다.", response));
    }
}
