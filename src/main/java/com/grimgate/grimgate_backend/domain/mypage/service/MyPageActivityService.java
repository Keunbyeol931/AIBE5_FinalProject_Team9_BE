package com.grimgate.grimgate_backend.domain.mypage.service;

import com.grimgate.grimgate_backend.domain.member.entity.Member;
import com.grimgate.grimgate_backend.domain.member.repository.MemberRepository;
import com.grimgate.grimgate_backend.domain.review.dto.ReviewResponse;
import com.grimgate.grimgate_backend.domain.review.dto.ReviewUpdateRequest;
import com.grimgate.grimgate_backend.domain.review.entity.Review;
import com.grimgate.grimgate_backend.domain.review.entity.ReviewImage;
import com.grimgate.grimgate_backend.domain.review.repository.ReviewImageRepository;
import com.grimgate.grimgate_backend.domain.review.repository.ReviewRepository;
import com.grimgate.grimgate_backend.domain.theme.entity.Theme;
import com.grimgate.grimgate_backend.domain.theme.repository.ThemeRepository;
import com.grimgate.grimgate_backend.global.exception.CustomException;
import com.grimgate.grimgate_backend.global.exception.ErrorCode;
import com.grimgate.grimgate_backend.global.security.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MyPageActivityService {

    private final MemberRepository memberRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final ThemeRepository themeRepository;

    // 내 후기 조회
    public List<ReviewResponse> getMyReviews(){
        Long accountId = SecurityUtil.getCurrentAccountId();
        Member member = memberRepository.findByAccount_Id(accountId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        return reviewRepository.findByMemberId(member.getId()).stream()
                .map(review -> {
                        List<String> imageUrls = reviewImageRepository.findByReview_Id(review.getId())
                                .stream()
                                .map(ReviewImage::getImageUrl)
                                .toList();

                      return ReviewResponse.builder()
                        .nickname(review.getMember().getAccount().getNickname())
                        .rating(review.getRating())
                        .horrorRating(review.getHorrorRating())
                        .difficultyRating(review.getDifficultyRating())
                        .tags(review.getTags())
                        .content(review.getContent())
                        .spoiler(review.getSpoiler())
                        .createdAt(review.getCreatedAt())
                              .imageUrls(imageUrls)
                        .build();
                })
                .toList();
    }

    // 내 후기 수정
    public ReviewResponse updateMyReview(Long reviewId, ReviewUpdateRequest request){
        Long accountId = SecurityUtil.getCurrentAccountId();
        Member member = memberRepository.findByAccount_Id(accountId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

        if (!review.getMember().getId().equals(member.getId())) {
            throw new CustomException(ErrorCode.REVIEW_NOT_OWNER);
        }

        // 이미지 최대 3장 검증
        if (request.getImageUrls() != null && request.getImageUrls().size() > 3) {
            throw new CustomException(ErrorCode.IMAGE_LIMIT_EXCEEDED);
        }

        review.update(request);

// 기존 이미지 삭제 후 새로 저장
        reviewImageRepository.deleteByReviewId(reviewId);
        if (request.getImageUrls() != null) {
            List<ReviewImage> images = request.getImageUrls().stream()
                    .map(url -> ReviewImage.builder()
                            .review(review)
                            .imageUrl(url)
                            .imageOrder(String.valueOf(request.getImageUrls().indexOf(url) + 1))
                            .build())
                    .toList();
            reviewImageRepository.saveAll(images);
        }

        // theme rating 재계산
        Theme theme = review.getTheme();
        List<Review> allReviews = reviewRepository.findByThemeId(theme.getId());
        double average = allReviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
        double newRating = Math.round(average * 10.0) / 10.0;
        theme.updateRating(newRating, allReviews.size());
        themeRepository.save(theme);

        // 저장된 이미지 조회
        List<String> imageUrls = reviewImageRepository.findByReview_Id(review.getId())
                .stream()
                .map(ReviewImage::getImageUrl)
                .toList();

        return ReviewResponse.builder()
                .nickname(review.getMember().getAccount().getNickname())
                .rating(review.getRating())
                .horrorRating(review.getHorrorRating())
                .difficultyRating(review.getDifficultyRating())
                .tags(review.getTags())
                .content(review.getContent())
                .spoiler(review.getSpoiler())
                .createdAt(review.getCreatedAt())
                .imageUrls(imageUrls)
                .build();

    }


    //내 후기 삭제
    public void deleteMyReview(Long reviewId) {
        Long accountId = SecurityUtil.getCurrentAccountId();

        Member member = memberRepository.findByAccount_Id(accountId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

        // 본인 확인
        if (!review.getMember().getId().equals(member.getId())) {
            throw new CustomException(ErrorCode.REVIEW_NOT_OWNER);
        }

        // 이미지 먼저 삭제
        reviewImageRepository.deleteByReviewId(reviewId);

        // 후기 삭제
        reviewRepository.delete(review);


        // theme rating 재계산
        Theme theme = review.getTheme();
        List<Review> remaining = reviewRepository.findByThemeId(theme.getId());
        double average = remaining.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
        double newRating = Math.round(average * 10.0) / 10.0;
        theme.updateRating(newRating, remaining.size());

    }


}
