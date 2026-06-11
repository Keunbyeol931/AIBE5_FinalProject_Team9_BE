package com.grimgate.grimgate_backend.domain.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.grimgate.grimgate_backend.domain.account.entity.Account;
import com.grimgate.grimgate_backend.domain.manager.entity.Manager;
import com.grimgate.grimgate_backend.domain.manager.repository.ManagerRepository;
import com.grimgate.grimgate_backend.domain.member.entity.Member;
import com.grimgate.grimgate_backend.domain.member.repository.MemberRepository;
import com.grimgate.grimgate_backend.domain.review.dto.ReviewReportCreateRequest;
import com.grimgate.grimgate_backend.domain.review.dto.ReviewReportHideRequest;
import com.grimgate.grimgate_backend.domain.review.dto.ReviewReportResponse;
import com.grimgate.grimgate_backend.domain.review.entity.Review;
import com.grimgate.grimgate_backend.domain.review.entity.ReviewReport;
import com.grimgate.grimgate_backend.domain.review.entity.ReviewReportStatus;
import com.grimgate.grimgate_backend.domain.review.repository.ReviewReportRepository;
import com.grimgate.grimgate_backend.domain.review.repository.ReviewRepository;
import com.grimgate.grimgate_backend.global.exception.CustomException;
import com.grimgate.grimgate_backend.global.exception.ErrorCode;
import com.grimgate.grimgate_backend.global.security.SecurityUtil;
import java.lang.reflect.Field;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * ReviewReportService 단위 테스트.
 *
 * <p>검증 포인트
 * <ul>
 *   <li>RR-001: 본인 후기 신고 차단, 중복 신고 차단, 정상 접수</li>
 *   <li>RR-002: 사장님 복구 처리, 이미 처리된 신고는 차단</li>
 *   <li>RR-003: 사장님 숨김 요청, 권한 없는 사장님 접근 차단</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class ReviewReportServiceTest {

    @Mock private ReviewReportRepository reviewReportRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private ManagerRepository managerRepository;

    @InjectMocks
    private ReviewReportService reviewReportService;

    // 신고자(일반 회원)
    private static final Long REPORTER_ACCOUNT_ID = 100L;
    private static final Long REPORTER_MEMBER_ID = 10L;
    // 후기 작성자
    private static final Long AUTHOR_MEMBER_ID = 20L;
    // 사장님(매니저)
    private static final Long OWNER_ACCOUNT_ID = 200L;
    private static final Long OWNER_MEMBER_ID = 30L;
    private static final Long OWNER_MANAGER_ID = 300L;

    private static final Long REVIEW_ID = 1L;
    private static final Long REPORT_ID = 500L;

    private Member reporterMember;
    private Member authorMember;
    private Member ownerMember;
    private Manager ownerManager;
    private Review review;

    private MockedStatic<SecurityUtil> securityUtilMock;

    @BeforeEach
    void setUp() throws Exception {
        Account reporterAccount = Account.builder().nickname("신고자").build();
        setId(reporterAccount, REPORTER_ACCOUNT_ID);
        reporterMember = Member.builder().account(reporterAccount).build();
        setId(reporterMember, REPORTER_MEMBER_ID);

        Account authorAccount = Account.builder().nickname("작성자").build();
        setId(authorAccount, 999L);
        authorMember = Member.builder().account(authorAccount).build();
        setId(authorMember, AUTHOR_MEMBER_ID);

        Account ownerAccount = Account.builder().nickname("사장님").build();
        setId(ownerAccount, OWNER_ACCOUNT_ID);
        ownerMember = Member.builder().account(ownerAccount).build();
        setId(ownerMember, OWNER_MEMBER_ID);
        ownerManager = Manager.builder().account(ownerAccount).build();
        setId(ownerManager, OWNER_MANAGER_ID);

        review = newInstance(Review.class);
        setField(review, "id", REVIEW_ID);
        setField(review, "member", authorMember);
        setField(review, "status", Review.STATUS_ACTIVE);

        securityUtilMock = Mockito.mockStatic(SecurityUtil.class);
    }

    @AfterEach
    void tearDown() {
        securityUtilMock.close();
    }

    /* ============================================================
     * RR-001 사용자 신고 접수
     * ============================================================ */

    @Test
    @DisplayName("RR-001 - 정상 신고 접수")
    void reportReview_success() {
        securityUtilMock.when(SecurityUtil::getCurrentAccountId).thenReturn(REPORTER_ACCOUNT_ID);
        when(memberRepository.findByAccount_Id(REPORTER_ACCOUNT_ID))
                .thenReturn(Optional.of(reporterMember));
        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(review));
        when(reviewReportRepository.existsByReview_IdAndReporter_Id(REVIEW_ID, REPORTER_MEMBER_ID))
                .thenReturn(false);
        when(reviewReportRepository.save(any(ReviewReport.class)))
                .thenAnswer(inv -> {
                    ReviewReport r = inv.getArgument(0);
                    setField(r, "id", REPORT_ID);
                    return r;
                });

        ReviewReportCreateRequest req = new ReviewReportCreateRequest();
        setField(req, "reason", "SPOILER");
        setField(req, "detail", "엔딩이 공개되어 있어요");

        ReviewReportResponse res = reviewReportService.reportReview(REVIEW_ID, req);

        assertThat(res.getId()).isEqualTo(REPORT_ID);
        assertThat(res.getReviewId()).isEqualTo(REVIEW_ID);
        assertThat(res.getReporterId()).isEqualTo(REPORTER_MEMBER_ID);
        assertThat(res.getStatus()).isEqualTo(ReviewReportStatus.PENDING_OWNER_REVIEW);
        verify(reviewReportRepository).save(any(ReviewReport.class));
    }

    @Test
    @DisplayName("RR-001 - 본인이 작성한 후기는 신고 불가")
    void reportReview_selfForbidden() {
        // 신고자 == 작성자 인 상황
        Member selfReporter = Member.builder().account(reporterMember.getAccount()).build();
        setField(selfReporter, "id", AUTHOR_MEMBER_ID); // 작성자 ID 와 동일

        securityUtilMock.when(SecurityUtil::getCurrentAccountId).thenReturn(REPORTER_ACCOUNT_ID);
        when(memberRepository.findByAccount_Id(REPORTER_ACCOUNT_ID))
                .thenReturn(Optional.of(selfReporter));
        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(review));

        ReviewReportCreateRequest req = new ReviewReportCreateRequest();
        setField(req, "reason", "SPOILER");

        assertThatThrownBy(() -> reviewReportService.reportReview(REVIEW_ID, req))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.REVIEW_REPORT_SELF_FORBIDDEN.getMessage());

        verify(reviewReportRepository, never()).save(any());
    }

    @Test
    @DisplayName("RR-001 - 중복 신고 차단")
    void reportReview_duplicate() {
        securityUtilMock.when(SecurityUtil::getCurrentAccountId).thenReturn(REPORTER_ACCOUNT_ID);
        when(memberRepository.findByAccount_Id(REPORTER_ACCOUNT_ID))
                .thenReturn(Optional.of(reporterMember));
        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(review));
        when(reviewReportRepository.existsByReview_IdAndReporter_Id(REVIEW_ID, REPORTER_MEMBER_ID))
                .thenReturn(true);

        ReviewReportCreateRequest req = new ReviewReportCreateRequest();
        setField(req, "reason", "SPOILER");

        assertThatThrownBy(() -> reviewReportService.reportReview(REVIEW_ID, req))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.REVIEW_REPORT_ALREADY_EXISTS.getMessage());

        verify(reviewReportRepository, never()).save(any());
    }

    @Test
    @DisplayName("RR-001 - 존재하지 않는 후기에 대한 신고는 REVIEW_NOT_FOUND")
    void reportReview_reviewNotFound() {
        securityUtilMock.when(SecurityUtil::getCurrentAccountId).thenReturn(REPORTER_ACCOUNT_ID);
        when(memberRepository.findByAccount_Id(REPORTER_ACCOUNT_ID))
                .thenReturn(Optional.of(reporterMember));
        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.empty());

        ReviewReportCreateRequest req = new ReviewReportCreateRequest();
        setField(req, "reason", "SPOILER");

        assertThatThrownBy(() -> reviewReportService.reportReview(REVIEW_ID, req))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.REVIEW_NOT_FOUND.getMessage());
    }

    /* ============================================================
     * RR-002 사장님 복구
     * ============================================================ */

    @Test
    @DisplayName("RR-002 - 사장님 복구 정상 처리 → 상태 OWNER_RESTORED")
    void restoreByOwner_success() {
        ReviewReport report = ReviewReport.create(review, reporterMember, "SPOILER", null);
        setField(report, "id", REPORT_ID);

        securityUtilMock.when(SecurityUtil::getCurrentAccountId).thenReturn(OWNER_ACCOUNT_ID);
        when(managerRepository.findByAccount_Id(OWNER_ACCOUNT_ID))
                .thenReturn(Optional.of(ownerManager));
        when(reviewReportRepository.findByIdForOwner(REPORT_ID, OWNER_MANAGER_ID))
                .thenReturn(Optional.of(report));
        when(memberRepository.findByAccount_Id(OWNER_ACCOUNT_ID))
                .thenReturn(Optional.of(ownerMember));

        ReviewReportResponse res = reviewReportService.restoreByOwner(REPORT_ID);

        assertThat(report.getStatus()).isEqualTo(ReviewReportStatus.OWNER_RESTORED);
        assertThat(report.getOwner()).isEqualTo(ownerMember);
        assertThat(report.getOwnerHandledAt()).isNotNull();
        assertThat(report.getResolvedAt()).isNotNull();
        // 후기 자체는 활성 상태 유지
        assertThat(review.getStatus()).isEqualTo(Review.STATUS_ACTIVE);
        assertThat(res.getStatus()).isEqualTo(ReviewReportStatus.OWNER_RESTORED);
    }

    @Test
    @DisplayName("RR-002 - 이미 처리된 신고는 NOT_PENDING_OWNER")
    void restoreByOwner_alreadyHandled() {
        ReviewReport report = ReviewReport.create(review, reporterMember, "SPOILER", null);
        setField(report, "id", REPORT_ID);
        // 이미 처리 완료된 상태로 강제
        setField(report, "status", ReviewReportStatus.OWNER_RESTORED);

        securityUtilMock.when(SecurityUtil::getCurrentAccountId).thenReturn(OWNER_ACCOUNT_ID);
        when(managerRepository.findByAccount_Id(OWNER_ACCOUNT_ID))
                .thenReturn(Optional.of(ownerManager));
        when(reviewReportRepository.findByIdForOwner(REPORT_ID, OWNER_MANAGER_ID))
                .thenReturn(Optional.of(report));

        assertThatThrownBy(() -> reviewReportService.restoreByOwner(REPORT_ID))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.REVIEW_REPORT_NOT_PENDING_OWNER.getMessage());
    }

    @Test
    @DisplayName("RR-002 - 권한 없는 사장님(다른 지점) 접근은 ACCESS_DENIED")
    void restoreByOwner_accessDenied() {
        securityUtilMock.when(SecurityUtil::getCurrentAccountId).thenReturn(OWNER_ACCOUNT_ID);
        when(managerRepository.findByAccount_Id(OWNER_ACCOUNT_ID))
                .thenReturn(Optional.of(ownerManager));
        // 권한 있는 단건 조회는 비어있고, exists는 true → 다른 사장님 소유의 신고
        when(reviewReportRepository.findByIdForOwner(REPORT_ID, OWNER_MANAGER_ID))
                .thenReturn(Optional.empty());
        when(reviewReportRepository.existsById(REPORT_ID)).thenReturn(true);

        assertThatThrownBy(() -> reviewReportService.restoreByOwner(REPORT_ID))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.REVIEW_REPORT_ACCESS_DENIED.getMessage());
    }

    @Test
    @DisplayName("RR-002 - 존재하지 않는 신고는 NOT_FOUND")
    void restoreByOwner_notFound() {
        securityUtilMock.when(SecurityUtil::getCurrentAccountId).thenReturn(OWNER_ACCOUNT_ID);
        when(managerRepository.findByAccount_Id(OWNER_ACCOUNT_ID))
                .thenReturn(Optional.of(ownerManager));
        when(reviewReportRepository.findByIdForOwner(REPORT_ID, OWNER_MANAGER_ID))
                .thenReturn(Optional.empty());
        when(reviewReportRepository.existsById(REPORT_ID)).thenReturn(false);

        assertThatThrownBy(() -> reviewReportService.restoreByOwner(REPORT_ID))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.REVIEW_REPORT_NOT_FOUND.getMessage());
    }

    /* ============================================================
     * RR-003 사장님 숨김 요청
     * ============================================================ */

    @Test
    @DisplayName("RR-003 - 사장님 숨김 요청 정상 처리 → 상태 REQUESTED_ADMIN_REVIEW + owner_reason 저장")
    void requestHideByOwner_success() {
        ReviewReport report = ReviewReport.create(review, reporterMember, "ABUSE", null);
        setField(report, "id", REPORT_ID);

        securityUtilMock.when(SecurityUtil::getCurrentAccountId).thenReturn(OWNER_ACCOUNT_ID);
        when(managerRepository.findByAccount_Id(OWNER_ACCOUNT_ID))
                .thenReturn(Optional.of(ownerManager));
        when(reviewReportRepository.findByIdForOwner(REPORT_ID, OWNER_MANAGER_ID))
                .thenReturn(Optional.of(report));
        when(memberRepository.findByAccount_Id(OWNER_ACCOUNT_ID))
                .thenReturn(Optional.of(ownerMember));

        ReviewReportHideRequest req = new ReviewReportHideRequest();
        setField(req, "ownerReason", "허위 비방성 내용");

        ReviewReportResponse res = reviewReportService.requestHideByOwner(REPORT_ID, req);

        assertThat(report.getStatus()).isEqualTo(ReviewReportStatus.REQUESTED_ADMIN_REVIEW);
        assertThat(report.getOwnerReason()).isEqualTo("허위 비방성 내용");
        assertThat(report.getOwnerHandledAt()).isNotNull();
        // 관리자 판정 전이므로 resolved_at 은 비어있어야 함
        assertThat(report.getResolvedAt()).isNull();
        assertThat(res.getOwnerReason()).isEqualTo("허위 비방성 내용");
    }

    @Test
    @DisplayName("RR-003 - 이미 처리된 신고는 NOT_PENDING_OWNER")
    void requestHideByOwner_alreadyHandled() {
        ReviewReport report = ReviewReport.create(review, reporterMember, "ABUSE", null);
        setField(report, "id", REPORT_ID);
        setField(report, "status", ReviewReportStatus.REQUESTED_ADMIN_REVIEW);

        securityUtilMock.when(SecurityUtil::getCurrentAccountId).thenReturn(OWNER_ACCOUNT_ID);
        when(managerRepository.findByAccount_Id(OWNER_ACCOUNT_ID))
                .thenReturn(Optional.of(ownerManager));
        when(reviewReportRepository.findByIdForOwner(REPORT_ID, OWNER_MANAGER_ID))
                .thenReturn(Optional.of(report));

        ReviewReportHideRequest req = new ReviewReportHideRequest();
        setField(req, "ownerReason", "재요청");

        assertThatThrownBy(() -> reviewReportService.requestHideByOwner(REPORT_ID, req))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.REVIEW_REPORT_NOT_PENDING_OWNER.getMessage());
    }

    /* ============================================================
     * 도메인 엔티티 자체 검증
     * ============================================================ */

    @Test
    @DisplayName("Review.hide() → status HIDDEN, restore() → ACTIVE")
    void reviewHideRestore() {
        Review r = newInstance(Review.class);
        setField(r, "status", Review.STATUS_ACTIVE);
        r.hide();
        assertThat(r.getStatus()).isEqualTo(Review.STATUS_HIDDEN);
        r.restore();
        assertThat(r.getStatus()).isEqualTo(Review.STATUS_ACTIVE);
    }

    /* ============================================================
     * 헬퍼
     * ============================================================ */

    private static void setId(Object target, Long id) {
        setField(target, "id", id);
    }

    @SuppressWarnings("unchecked")
    private static <T> T newInstance(Class<T> clazz) {
        try {
            var ctor = clazz.getDeclaredConstructor();
            ctor.setAccessible(true);
            return (T) ctor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field f = findField(target.getClass(), fieldName);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Field findField(Class<?> clazz, String name) throws NoSuchFieldException {
        Class<?> c = clazz;
        while (c != null) {
            try {
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                c = c.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }
}
