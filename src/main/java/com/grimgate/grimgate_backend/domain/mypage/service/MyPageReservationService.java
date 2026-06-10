package com.grimgate.grimgate_backend.domain.mypage.service;

import com.grimgate.grimgate_backend.domain.member.entity.Member;
import com.grimgate.grimgate_backend.domain.member.repository.MemberRepository;
import com.grimgate.grimgate_backend.domain.reservation.entity.Reservation;
import com.grimgate.grimgate_backend.domain.reservation.entity.ReservationStatus;
import com.grimgate.grimgate_backend.domain.reservation.repository.ReservationRepository;
import com.grimgate.grimgate_backend.domain.review.dto.ReviewCreateRequest;
import com.grimgate.grimgate_backend.domain.review.dto.ReviewResponse;
import com.grimgate.grimgate_backend.domain.review.entity.Review;
import com.grimgate.grimgate_backend.domain.review.entity.ReviewImage;
import com.grimgate.grimgate_backend.domain.review.repository.ReviewImageRepository;
import com.grimgate.grimgate_backend.domain.review.repository.ReviewRepository;
import com.grimgate.grimgate_backend.domain.theme.entity.Theme;
import com.grimgate.grimgate_backend.global.exception.CustomException;
import com.grimgate.grimgate_backend.global.exception.ErrorCode;
import com.grimgate.grimgate_backend.global.security.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MyPageReservationService {

    private final ReservationRepository reservationRepository;
    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;
    private final ReviewImageRepository reviewImageRepository;


    //후기 작성
    public ReviewResponse createReview( ReviewCreateRequest request) {
        Long accountId = SecurityUtil.getCurrentAccountId();

        Member member = memberRepository.findByAccount_Id(accountId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Reservation reservation = reservationRepository.findById(request.getReservationId())
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        //본인 예약인지 확인
        if (!reservation.getMember().getId().equals(member.getId())) {
            throw new CustomException(ErrorCode.REVIEW_NOT_OWNER);
        }
        //지난 예약인지 확인
        if (!reservation.getTimeSlot().getSlotDate().isBefore(LocalDate.now())) {
            throw new CustomException(ErrorCode.RESERVATION_NOT_COMPLETED);
        }

        // 취소된 예약 검증 추가
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new CustomException(ErrorCode.RESERVATION_CANCELLED);
        }
        //중복 후기 확인
        if (reviewRepository.existsByReservationId(request.getReservationId())) {
            throw new CustomException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        Theme theme = reservation.getTimeSlot().getTheme();

        // 이미지 최대 3장 검증
        if (request.getImageUrls() != null && request.getImageUrls().size() > 3) {

            throw new CustomException(ErrorCode.IMAGE_LIMIT_EXCEEDED);
        }

        Review review = Review.create(member, theme, reservation, request);
        reviewRepository.save(review);

        // 이미지 저장
        if (request.getImageUrls() != null ) {
            List<ReviewImage> images = request.getImageUrls().stream()
                    .map(url -> ReviewImage.builder()
                            .review(review)
                            .imageUrl(url)
                            .imageOrder(String.valueOf(request.getImageUrls().indexOf(url) + 1))
                            .build())
                    .toList();
            reviewImageRepository.saveAll(images);
        }

        // 저장된 이미지 조회
        List<String> imageUrls = reviewImageRepository.findByReview_Id(review.getId())
                .stream()
                .map(ReviewImage::getImageUrl)
                .toList();

        return ReviewResponse.builder()
                .nickname(member.getAccount().getNickname())
                .rating(review.getRating())
                .horrorRating(review.getHorrorRating())
                .difficultyRating(review.getDifficultyRating())
                .tags(review.getTags())
                .content(review.getContent())
                .spoiler(review.getSpoiler())
                .imageUrls(imageUrls)
                .build();
    }


}
