package com.grimgate.grimgate_backend.domain.reservation.repository;

import com.grimgate.grimgate_backend.domain.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 예약(Reservation) 엔티티에 대한 데이터베이스 액세스 처리를 담당하는 Repository 인터페이스입니다.
 */
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
}
