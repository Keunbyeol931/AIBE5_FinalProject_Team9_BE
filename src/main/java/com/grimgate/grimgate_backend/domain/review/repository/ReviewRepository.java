package com.grimgate.grimgate_backend.domain.review.repository;

import com.grimgate.grimgate_backend.domain.review.entity.Review;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByThemeId(Long themeId, Pageable pageable);
    List<Review> findByThemeId(Long themeId);
    // 내가 쓴 후기 목록(마이페이지)
    List<Review> findByMemberId(Long memberId);
    // 예약에 이미 후기 있는지 확인 (중복 방지)
    boolean existsByReservationId(Long reservationId);
    void deleteByThemeId(Long themeId);

}
