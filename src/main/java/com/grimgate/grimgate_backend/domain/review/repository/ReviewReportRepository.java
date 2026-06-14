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
     * 사장님이 운영하는 지점들의 테마에 달린 후기에 대한 신고 목록 조회 (상태 필터).
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
              AND rr.status = :status
            """,
            countQuery = """
            SELECT COUNT(rr)
            FROM ReviewReport rr
              JOIN rr.review r
              JOIN r.theme t
              JOIN t.branch b
            WHERE b.managerId = :managerId
              AND rr.status = :status
            """)
    Page<ReviewReport> findOwnerReportsByStatus(
            @Param("managerId") Long managerId,
            @Param("status") ReviewReportStatus status,
            Pageable pageable
    );

    /**
     * 사장님 신고 목록 전체 조회 (상태 필터 없음).
     *
     * <p>JPQL에서 enum null 파라미터 타입 추론 이슈를 피하기 위해 메서드 분리.
     */
    @Query(value = """
            SELECT rr
            FROM ReviewReport rr
              JOIN FETCH rr.review r
              JOIN FETCH r.theme t
              JOIN FETCH t.branch b
            WHERE b.managerId = :managerId
            """,
            countQuery = """
            SELECT COUNT(rr)
            FROM ReviewReport rr
              JOIN rr.review r
              JOIN r.theme t
              JOIN t.branch b
            WHERE b.managerId = :managerId
            """)
    Page<ReviewReport> findOwnerReportsAll(
            @Param("managerId") Long managerId,
            Pageable pageable
    );

    /** 상태 파라미터에 따라 적절한 메서드로 디스패치. */
    default Page<ReviewReport> findOwnerReports(Long managerId, ReviewReportStatus status, Pageable pageable) {
        return (status == null)
                ? findOwnerReportsAll(managerId, pageable)
                : findOwnerReportsByStatus(managerId, status, pageable);
    }

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
