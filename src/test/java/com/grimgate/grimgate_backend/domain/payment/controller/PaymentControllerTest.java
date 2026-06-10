package com.grimgate.grimgate_backend.domain.payment.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grimgate.grimgate_backend.domain.payment.dto.PaymentReadyRequest;
import com.grimgate.grimgate_backend.domain.payment.dto.PaymentReadyResponse;
import com.grimgate.grimgate_backend.domain.payment.entity.PaymentStatus;
import com.grimgate.grimgate_backend.domain.payment.service.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("POST /api/payments/ready - 결제 준비 요청 성공")
    void readyPayment_Success() throws Exception {
        // given
        PaymentReadyRequest request = PaymentReadyRequest.builder()
                .reservationId(100L)
                .amount(22000)
                .build();

        PaymentReadyResponse response = PaymentReadyResponse.builder()
                .paymentId(1L)
                .reservationId(100L)
                .orderId("PAY-ORDER-123")
                .amount(22000)
                .status(PaymentStatus.PAY_PENDING)
                .orderName("공포의 방")
                .customerName("테스터")
                .customerEmail("test@test.com")
                .build();

        when(paymentService.readyPayment(any(PaymentReadyRequest.class))).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/payments/ready")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("결제 준비가 완료되었습니다."))
                .andExpect(jsonPath("$.data.paymentId").value(1))
                .andExpect(jsonPath("$.data.reservationId").value(100))
                .andExpect(jsonPath("$.data.orderId").value("PAY-ORDER-123"))
                .andExpect(jsonPath("$.data.amount").value(22000))
                .andExpect(jsonPath("$.data.status").value("PAY_PENDING"))
                .andExpect(jsonPath("$.data.orderName").value("공포의 방"))
                .andExpect(jsonPath("$.data.customerName").value("테스터"))
                .andExpect(jsonPath("$.data.customerEmail").value("test@test.com"));
    }

    @Test
    @DisplayName("POST /api/payments/ready - 예약 ID 누락 시 400 BAD_REQUEST 반환")
    void readyPayment_ValidationFailure_MissingReservationId() throws Exception {
        // given
        PaymentReadyRequest request = PaymentReadyRequest.builder()
                .amount(22000)
                .build();

        // when & then
        mockMvc.perform(post("/api/payments/ready")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("예약 ID는 필수입니다."));
    }

    @Test
    @DisplayName("POST /api/payments/ready - 결제 금액 0 이하일 때 400 BAD_REQUEST 반환")
    void readyPayment_ValidationFailure_InvalidAmount() throws Exception {
        // given
        PaymentReadyRequest request = PaymentReadyRequest.builder()
                .reservationId(100L)
                .amount(-1000)
                .build();

        // when & then
        mockMvc.perform(post("/api/payments/ready")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("결제 금액은 0보다 커야 합니다."));
    }
}
