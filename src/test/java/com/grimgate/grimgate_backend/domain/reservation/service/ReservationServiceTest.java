package com.grimgate.grimgate_backend.domain.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.grimgate.grimgate_backend.domain.member.entity.Member;
import com.grimgate.grimgate_backend.domain.member.repository.MemberRepository;
import com.grimgate.grimgate_backend.domain.reservation.dto.ReservationCreateRequest;
import com.grimgate.grimgate_backend.domain.reservation.dto.ReservationCreateResponse;
import com.grimgate.grimgate_backend.domain.reservation.entity.Reservation;
import com.grimgate.grimgate_backend.domain.reservation.entity.ReservationStatus;
import com.grimgate.grimgate_backend.domain.reservation.repository.ReservationRepository;
import com.grimgate.grimgate_backend.domain.theme.entity.Theme;
import com.grimgate.grimgate_backend.domain.theme.entity.TimeSlot;
import com.grimgate.grimgate_backend.domain.theme.entity.TimeSlotStatus;
import com.grimgate.grimgate_backend.domain.theme.repository.TimeSlotRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private TimeSlotRepository timeSlotRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    @DisplayName("예약 생성 성공 - 모든 검증을 통과하고 예약이 PENDING_PAYMENT 상태로 저장된다")
    void createReservation_Success() {
        // given
        Long memberId = 1L;
        Long timeSlotId = 10L;
        String holdToken = "hold-token-123";
        int peopleCount = 3;

        ReservationCreateRequest request = ReservationCreateRequest.builder()
                .memberId(memberId)
                .timeSlotId(timeSlotId)
                .holdToken(holdToken)
                .peopleCount(peopleCount)
                .build();

        String redisKey = "hold:slot:" + timeSlotId;
        String redisValue = memberId + ":" + holdToken;

        Theme theme = Theme.builder()
                .id(100L)
                .minPeople(2)
                .maxPeople(5)
                .price(22000)
                .build();

        TimeSlot timeSlot = TimeSlot.builder()
                .id(timeSlotId)
                .theme(theme)
                .status(TimeSlotStatus.SLOT_AVAILABLE)
                .build();

        Member member = Member.builder()
                .id(memberId)
                .build();

        Reservation savedReservation = Reservation.builder()
                .id(50L)
                .timeSlot(timeSlot)
                .member(member)
                .peopleCount(peopleCount)
                .totalPrice(66000)
                .status(ReservationStatus.PENDING_PAYMENT)
                .build();

        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(redisKey)).thenReturn(redisValue);
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(timeSlotRepository.findById(timeSlotId)).thenReturn(Optional.of(timeSlot));
        when(timeSlotRepository.updateStatus(eq(timeSlotId), eq(TimeSlotStatus.SLOT_HELD), eq(TimeSlotStatus.SLOT_AVAILABLE), any(LocalDateTime.class)))
                .thenReturn(1);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(savedReservation);
        when(stringRedisTemplate.execute(any(RedisScript.class), anyList(), any())).thenReturn(1L);

        // when
        ReservationCreateResponse response = reservationService.createReservation(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getReservationId()).isEqualTo(50L);
        assertThat(response.getTimeSlotId()).isEqualTo(timeSlotId);
        assertThat(response.getMemberId()).isEqualTo(memberId);
        assertThat(response.getStatus()).isEqualTo("PENDING_PAYMENT");
        assertThat(response.getPeopleCount()).isEqualTo(peopleCount);
        assertThat(response.getTotalPrice()).isEqualTo(66000);

        verify(timeSlotRepository).updateStatus(eq(timeSlotId), eq(TimeSlotStatus.SLOT_HELD), eq(TimeSlotStatus.SLOT_AVAILABLE), any(LocalDateTime.class));
        verify(reservationRepository).save(any(Reservation.class));
        verify(stringRedisTemplate).execute(any(RedisScript.class), anyList(), any());
    }

    @Test
    @DisplayName("예약 생성 실패 - Redis 선점 정보가 존재하지 않아 404 NOT_FOUND 발생")
    void createReservation_RedisHoldNotFound() {
        // given
        Long memberId = 1L;
        Long timeSlotId = 10L;
        ReservationCreateRequest request = ReservationCreateRequest.builder()
                .memberId(memberId)
                .timeSlotId(timeSlotId)
                .holdToken("hold-token-123")
                .peopleCount(3)
                .build();

        String redisKey = "hold:slot:" + timeSlotId;

        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(redisKey)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException statusEx = (ResponseStatusException) ex;
                    assertThat(statusEx.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(statusEx.getReason()).isEqualTo("선점 정보가 존재하지 않습니다.");
                });

        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    @DisplayName("예약 생성 실패 - Redis 선점 정보(토큰)가 불일치하여 409 CONFLICT 발생")
    void createReservation_RedisHoldMismatch() {
        // given
        Long memberId = 1L;
        Long timeSlotId = 10L;
        ReservationCreateRequest request = ReservationCreateRequest.builder()
                .memberId(memberId)
                .timeSlotId(timeSlotId)
                .holdToken("hold-token-123")
                .peopleCount(3)
                .build();

        String redisKey = "hold:slot:" + timeSlotId;
        String redisValue = memberId + ":different-token";

        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(redisKey)).thenReturn(redisValue);

        // when & then
        assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException statusEx = (ResponseStatusException) ex;
                    assertThat(statusEx.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(statusEx.getReason()).isEqualTo("선점 정보가 일치하지 않습니다.");
                });

        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    @DisplayName("예약 생성 실패 - 회원 정보가 존재하지 않아 404 NOT_FOUND 발생")
    void createReservation_MemberNotFound() {
        // given
        Long memberId = 1L;
        Long timeSlotId = 10L;
        String holdToken = "hold-token-123";
        ReservationCreateRequest request = ReservationCreateRequest.builder()
                .memberId(memberId)
                .timeSlotId(timeSlotId)
                .holdToken(holdToken)
                .peopleCount(3)
                .build();

        String redisKey = "hold:slot:" + timeSlotId;
        String redisValue = memberId + ":" + holdToken;

        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(redisKey)).thenReturn(redisValue);
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException statusEx = (ResponseStatusException) ex;
                    assertThat(statusEx.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(statusEx.getReason()).isEqualTo("회원을 찾을 수 없습니다.");
                });
    }

    @Test
    @DisplayName("예약 생성 실패 - 타임슬롯 정보가 존재하지 않아 404 NOT_FOUND 발생")
    void createReservation_TimeSlotNotFound() {
        // given
        Long memberId = 1L;
        Long timeSlotId = 10L;
        String holdToken = "hold-token-123";
        ReservationCreateRequest request = ReservationCreateRequest.builder()
                .memberId(memberId)
                .timeSlotId(timeSlotId)
                .holdToken(holdToken)
                .peopleCount(3)
                .build();

        String redisKey = "hold:slot:" + timeSlotId;
        String redisValue = memberId + ":" + holdToken;

        Member member = Member.builder().id(memberId).build();

        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(redisKey)).thenReturn(redisValue);
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(timeSlotRepository.findById(timeSlotId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException statusEx = (ResponseStatusException) ex;
                    assertThat(statusEx.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(statusEx.getReason()).isEqualTo("존재하지 않는 슬롯입니다.");
                });
    }

    @Test
    @DisplayName("예약 생성 실패 - 타임슬롯의 상태가 SLOT_AVAILABLE이 아니라서 409 CONFLICT 발생")
    void createReservation_TimeSlotNotAvailable() {
        // given
        Long memberId = 1L;
        Long timeSlotId = 10L;
        String holdToken = "hold-token-123";
        ReservationCreateRequest request = ReservationCreateRequest.builder()
                .memberId(memberId)
                .timeSlotId(timeSlotId)
                .holdToken(holdToken)
                .peopleCount(3)
                .build();

        String redisKey = "hold:slot:" + timeSlotId;
        String redisValue = memberId + ":" + holdToken;

        Member member = Member.builder().id(memberId).build();
        TimeSlot timeSlot = TimeSlot.builder()
                .id(timeSlotId)
                .status(TimeSlotStatus.SLOT_HELD)
                .build();

        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(redisKey)).thenReturn(redisValue);
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(timeSlotRepository.findById(timeSlotId)).thenReturn(Optional.of(timeSlot));

        // when & then
        assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException statusEx = (ResponseStatusException) ex;
                    assertThat(statusEx.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(statusEx.getReason()).isEqualTo("예약 가능한 슬롯 상태가 아닙니다.");
                });
    }

    @Test
    @DisplayName("예약 생성 실패 - 인원 수가 테마의 예약 가능 범위를 벗어나 400 BAD_REQUEST 발생")
    void createReservation_PeopleCountOutOfRange() {
        // given
        Long memberId = 1L;
        Long timeSlotId = 10L;
        String holdToken = "hold-token-123";
        ReservationCreateRequest request = ReservationCreateRequest.builder()
                .memberId(memberId)
                .timeSlotId(timeSlotId)
                .holdToken(holdToken)
                .peopleCount(6) // max가 5인 상황에서 6명 요청
                .build();

        String redisKey = "hold:slot:" + timeSlotId;
        String redisValue = memberId + ":" + holdToken;

        Theme theme = Theme.builder()
                .id(100L)
                .minPeople(2)
                .maxPeople(5)
                .price(22000)
                .build();

        TimeSlot timeSlot = TimeSlot.builder()
                .id(timeSlotId)
                .theme(theme)
                .status(TimeSlotStatus.SLOT_AVAILABLE)
                .build();

        Member member = Member.builder().id(memberId).build();

        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(redisKey)).thenReturn(redisValue);
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(timeSlotRepository.findById(timeSlotId)).thenReturn(Optional.of(timeSlot));

        // when & then
        assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException statusEx = (ResponseStatusException) ex;
                    assertThat(statusEx.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(statusEx.getReason()).isEqualTo("인원 수 범위를 초과했습니다.");
                });
    }

    @Test
    @DisplayName("예약 생성 실패 - 슬롯 상태를 HELD로 변경하는 과정에서 경쟁으로 인해 업데이트 결과가 0이라서 409 CONFLICT 발생")
    void createReservation_TimeSlotUpdateConflict() {
        // given
        Long memberId = 1L;
        Long timeSlotId = 10L;
        String holdToken = "hold-token-123";
        ReservationCreateRequest request = ReservationCreateRequest.builder()
                .memberId(memberId)
                .timeSlotId(timeSlotId)
                .holdToken(holdToken)
                .peopleCount(3)
                .build();

        String redisKey = "hold:slot:" + timeSlotId;
        String redisValue = memberId + ":" + holdToken;

        Theme theme = Theme.builder()
                .id(100L)
                .minPeople(2)
                .maxPeople(5)
                .price(22000)
                .build();

        TimeSlot timeSlot = TimeSlot.builder()
                .id(timeSlotId)
                .theme(theme)
                .status(TimeSlotStatus.SLOT_AVAILABLE)
                .build();

        Member member = Member.builder().id(memberId).build();

        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(redisKey)).thenReturn(redisValue);
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(timeSlotRepository.findById(timeSlotId)).thenReturn(Optional.of(timeSlot));
        when(timeSlotRepository.updateStatus(eq(timeSlotId), eq(TimeSlotStatus.SLOT_HELD), eq(TimeSlotStatus.SLOT_AVAILABLE), any(LocalDateTime.class)))
                .thenReturn(0); // 0개 행 업데이트됨

        // when & then
        assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException statusEx = (ResponseStatusException) ex;
                    assertThat(statusEx.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(statusEx.getReason()).isEqualTo("이미 다른 사용자가 선점 중이거나 예약이 완료된 슬롯입니다.");
                });
    }
}
