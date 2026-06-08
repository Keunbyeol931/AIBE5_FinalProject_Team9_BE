package com.grimgate.grimgate_backend.domain.mypage.controller;

import com.grimgate.grimgate_backend.domain.mypage.service.MyPageReservationService;
import com.grimgate.grimgate_backend.domain.mypage.service.MyPageActivityService;
import com.grimgate.grimgate_backend.domain.review.dto.ReviewCreateRequest;
import com.grimgate.grimgate_backend.domain.review.dto.ReviewResponse;
import com.grimgate.grimgate_backend.domain.review.dto.ReviewUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MyPageController {

    private final MyPageReservationService mypageReservationService;
    private final MyPageActivityService mypageActivityService;

//후기 생성
 @PostMapping("/reviews")
   public ResponseEntity<ReviewResponse> createReview(
           @Valid @RequestBody ReviewCreateRequest request) {
     return ResponseEntity.ok(mypageReservationService.createReview(request));
 }

 // 내 후기 조회
    @GetMapping("/reviews")
    public ResponseEntity<List<ReviewResponse>> getMyReviews(
            @RequestParam(name = "user_id") String userId) {
        return ResponseEntity.ok(mypageActivityService.getMyReviews());
    }

 //내 후기 수정
 @PatchMapping("/reviews/{reviewId}")
 public ResponseEntity<ReviewResponse> updateMyReview(
         @PathVariable Long reviewId,
         @Valid @RequestBody ReviewUpdateRequest request){
   return ResponseEntity.ok(mypageActivityService.updateMyReview(reviewId, request));
 }


 //내 후기 삭제
    @DeleteMapping("/reviews/{reviewId}")
 public ResponseEntity<Void> deleteMyReview(@PathVariable Long reviewId){
        mypageActivityService.deleteMyReview(reviewId);
     return ResponseEntity.ok().build();
 }
}
