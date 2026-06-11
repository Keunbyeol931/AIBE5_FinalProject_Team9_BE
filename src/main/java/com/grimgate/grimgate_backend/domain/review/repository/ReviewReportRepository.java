package com.grimgate.grimgate_backend.domain.review.repository;

import com.grimgate.grimgate_backend.domain.review.entity.ReviewReport;
import com.grimgate.grimgate_backend.domain.review.entity.ReviewReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewReportRepository extends JpaRepository<ReviewReport, Long> {

    /** (review, reporter) 중복 신고 차단용 */
    boolean existsByReview_IdAndReporter_Id(Long reviewId, Long reporterId);

    /**
     * 사장님이 운영하는 지점들의 테마에 달린 후기에 대한 신고 목록 조회.
     * status 가 null 이면 전체, 아니면 해당 상태만 조회.
     *
     * <p>N+1 방지를 위해 review, theme 까지 fetch join.
     */
    @Query(value = """
            SELECT rr
            FROM ReviewReport rr
              JOIN FETCH rr.review r
              JOIN FETCH r.theme t
              JOIN FETCH t.branch b
            WHERE b.managerId = :managerId
              AND (:status IS NULL OR rr.status = :status)
            """,
            countQuery = """
            SELECT COUNT(rr)
            FROM ReviewReport rr
              JOIN rr.review r
              JOIN r.theme t
              JOIN t.branch b
            WHERE b.managerId = :managerId
              AND (:status IS NULL OR rr.status = :status)
            """)
    Page<ReviewReport> findOwnerReports(
            @Param("managerId") Long managerId,
            @Param("status") ReviewReportStatus status,
            Pageable pageable
    );

    /** 사장님 권한 검증을 동시에 수행하는 단건 조회. (소유 지점 매니저만 조회 가능) */
    @Query("""
            SELECT rr
            FROM ReviewReport rr
              JOIN FETCH rr.review r
              JOIN FETCH r.theme t
              JOIN FETCH t.branch b
            WHERE rr.id = :reportId
              AND b.managerId = :managerId
            """)
    Optional<ReviewReport> findByIdForOwner(
            @Param("reportId") Long reportId,
            @Param("managerId") Long managerId
    );

    List<ReviewReport> findAllByReview_Id(Long reviewId);
}
