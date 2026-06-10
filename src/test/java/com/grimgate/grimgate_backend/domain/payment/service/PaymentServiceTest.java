package com.grimgate.grimgate_backend.domain.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.grimgate.grimgate_backend.domain.account.entity.Account;
import com.grimgate.grimgate_backend.domain.member.entity.Member;
import com.grimgate.grimgate_backend.domain.member.repository.MemberRepository;
import com.grimgate.grimgate_backend.domain.payment.dto.PaymentReadyRequest;
import com.grimgate.grimgate_backend.domain.payment.dto.PaymentReadyResponse;
import com.grimgate.grimgate_backend.domain.payment.entity.Payment;
import com.grimgate.grimgate_backend.domain.payment.entity.PaymentStatus;
import com.grimgate.grimgate_backend.domain.payment.repository.PaymentRepository;
import com.grimgate.grimgate_backend.domain.reservation.entity.Reservation;
import com.grimgate.grimgate_backend.domain.reservation.entity.ReservationStatus;
import com.grimgate.grimgate_backend.domain.reservation.repository.ReservationRepository;
import com.grimgate.grimgate_backend.domain.theme.entity.Theme;
import com.grimgate.grimgate_backend.domain.theme.entity.TimeSlot;
import com.grimgate.grimgate_backend.global.exception.CustomException;
import com.grimgate.grimgate_backend.global.exception.ErrorCode;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private PaymentService paymentService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private Member setupSecurityContextAndMember(Long accountId, Long memberId, String nickname, String email) {
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        UserDetails userDetails = Mockito.mock(UserDetails.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn(String.valueOf(accountId));

        SecurityContextHolder.setContext(securityContext);

        Account account = Account.builder()
                .id(accountId)
                .nickname(nickname)
                .email(email)
                .build();

        Member member = Member.builder()
                .id(memberId)
                .account(account)
                .build();

        when(memberRepository.findByAccount_Id(accountId))
                .thenReturn(Optional.of(member));

        return member;
    }

    @Test
    @DisplayName("결제 준비 성공 - 검증 항목을 모두 통과하면 PAY_PENDING 상태로 결제가 저장된다")
    void readyPayment_Success() {
        // given
        Long accountId = 1L;
        Long memberId = 10L;
        Long reservationId = 100L;
        Integer amount = 22000;

        Member member = setupSecurityContextAndMember(accountId, memberId, "테스터", "test@test.com");

        Theme theme = Theme.builder()
                .title("공포의 방")
                .build();

        TimeSlot timeSlot = TimeSlot.builder()
                .theme(theme)
                .build();

        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .member(member)
                .timeSlot(timeSlot)
                .totalPrice(amount)
                .status(ReservationStatus.PENDING_PAYMENT)
                .build();

        PaymentReadyRequest request = PaymentReadyRequest.builder()
                .reservationId(reservationId)
                .amount(amount)
                .build();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(paymentRepository.findByReservationId(reservationId)).thenReturn(Optional.empty());

        Payment mockSavedPayment = Payment.builder()
                .id(1L)
                .reservation(reservation)
                .member(member)
                .amount(amount)
                .orderId("PAY-ORDER-123")
                .status(PaymentStatus.PAY_PENDING)
                .build();
        when(paymentRepository.save(any(Payment.class))).thenReturn(mockSavedPayment);

        // when
        PaymentReadyResponse response = paymentService.readyPayment(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getPaymentId()).isEqualTo(1L);
        assertThat(response.getReservationId()).isEqualTo(reservationId);
        assertThat(response.getOrderId()).isEqualTo("PAY-ORDER-123");
        assertThat(response.getAmount()).isEqualTo(amount);
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.PAY_PENDING);
        assertThat(response.getOrderName()).isEqualTo("공포의 방");
        assertThat(response.getCustomerName()).isEqualTo("테스터");
        assertThat(response.getCustomerEmail()).isEqualTo("test@test.com");

        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("결제 준비 실패 - 회원을 찾을 수 없으면 MEMBER_NOT_FOUND 에러를 발생시킨다")
    void readyPayment_MemberNotFound() {
        // given
        Long accountId = 1L;
        PaymentReadyRequest request = PaymentReadyRequest.builder()
                .reservationId(100L)
                .amount(22000)
                .build();

        // Security context setup
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        UserDetails userDetails = Mockito.mock(UserDetails.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn(String.valueOf(accountId));
        SecurityContextHolder.setContext(securityContext);

        when(memberRepository.findByAccount_Id(accountId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> paymentService.readyPayment(request))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("결제 준비 실패 - 예약을 찾을 수 없으면 RESERVATION_NOT_FOUND 에러를 발생시킨다")
    void readyPayment_ReservationNotFound() {
        // given
        Long accountId = 1L;
        Long memberId = 10L;
        Long reservationId = 100L;
        setupSecurityContextAndMember(accountId, memberId, "테스터", "test@test.com");

        PaymentReadyRequest request = PaymentReadyRequest.builder()
                .reservationId(reservationId)
                .amount(22000)
                .build();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> paymentService.readyPayment(request))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.RESERVATION_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("결제 준비 실패 - 로그인된 회원이 예약의 소유자가 아니면 FORBIDDEN 에러를 발생시킨다")
    void readyPayment_Forbidden() {
        // given
        Long accountId = 1L;
        Long memberId = 10L;
        Long reservationId = 100L;
        setupSecurityContextAndMember(accountId, memberId, "테스터", "test@test.com");

        Member differentMember = Member.builder()
                .id(999L)
                .build();

        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .member(differentMember)
                .build();

        PaymentReadyRequest request = PaymentReadyRequest.builder()
                .reservationId(reservationId)
                .amount(22000)
                .build();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        // when & then
        assertThatThrownBy(() -> paymentService.readyPayment(request))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException customEx = (CustomException) ex;
                    assertThat(customEx.getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                });
    }

    @Test
    @DisplayName("결제 준비 실패 - 예약 상태가 PENDING_PAYMENT가 아니면 INVALID_RESERVATION_STATUS 에러를 발생시킨다")
    void readyPayment_InvalidReservationStatus() {
        // given
        Long accountId = 1L;
        Long memberId = 10L;
        Long reservationId = 100L;
        Member member = setupSecurityContextAndMember(accountId, memberId, "테스터", "test@test.com");

        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .member(member)
                .status(ReservationStatus.CONFIRMED) // 결제 대기가 아님
                .build();

        PaymentReadyRequest request = PaymentReadyRequest.builder()
                .reservationId(reservationId)
                .amount(22000)
                .build();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        // when & then
        assertThatThrownBy(() -> paymentService.readyPayment(request))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.INVALID_RESERVATION_STATUS.getMessage());
    }

    @Test
    @DisplayName("결제 준비 실패 - 요청 금액과 예약 총 금액이 일치하지 않으면 PAYMENT_AMOUNT_MISMATCH 에러를 발생시킨다")
    void readyPayment_AmountMismatch() {
        // given
        Long accountId = 1L;
        Long memberId = 10L;
        Long reservationId = 100L;
        Member member = setupSecurityContextAndMember(accountId, memberId, "테스터", "test@test.com");

        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .member(member)
                .totalPrice(22000) // 실제 금액
                .status(ReservationStatus.PENDING_PAYMENT)
                .build();

        PaymentReadyRequest request = PaymentReadyRequest.builder()
                .reservationId(reservationId)
                .amount(33000) // 요청 금액 (위변조 시도)
                .build();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        // when & then
        assertThatThrownBy(() -> paymentService.readyPayment(request))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.PAYMENT_AMOUNT_MISMATCH.getMessage());
    }

    @Test
    @DisplayName("결제 준비 실패 - 이미 해당 예약에 결제 데이터가 존재하면 PAYMENT_ALREADY_EXISTS 에러를 발생시킨다")
    void readyPayment_AlreadyExists() {
        // given
        Long accountId = 1L;
        Long memberId = 10L;
        Long reservationId = 100L;
        Integer amount = 22000;
        Member member = setupSecurityContextAndMember(accountId, memberId, "테스터", "test@test.com");

        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .member(member)
                .totalPrice(amount)
                .status(ReservationStatus.PENDING_PAYMENT)
                .build();

        PaymentReadyRequest request = PaymentReadyRequest.builder()
                .reservationId(reservationId)
                .amount(amount)
                .build();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        Payment existingPayment = Payment.builder()
                .id(5L)
                .build();
        when(paymentRepository.findByReservationId(reservationId)).thenReturn(Optional.of(existingPayment));

        // when & then
        assertThatThrownBy(() -> paymentService.readyPayment(request))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.PAYMENT_ALREADY_EXISTS.getMessage());
    }
}
