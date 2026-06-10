package com.grimgate.grimgate_backend.domain.mypage.controller;

import com.grimgate.grimgate_backend.domain.mypage.service.MyPageReservationService;
import com.grimgate.grimgate_backend.domain.mypage.service.MyPageActivityService;
import com.grimgate.grimgate_backend.domain.review.dto.ReviewCreateRequest;
import com.grimgate.grimgate_backend.domain.review.dto.ReviewResponse;
import com.grimgate.grimgate_backend.domain.review.dto.ReviewUpdateRequest;
import com.grimgate.grimgate_backend.global.response.ApiResponse;
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
   public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
           @Valid @RequestBody ReviewCreateRequest request) {
     return ResponseEntity.ok(ApiResponse.success("후기 작성 성공", mypageReservationService.createReview(request)));
 }

 // 내 후기 조회
    @GetMapping("/reviews")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getMyReviews() {
        return ResponseEntity.ok(ApiResponse.success("후기 조회 성공", mypageActivityService.getMyReviews()));
    }

 //내 후기 수정
 @PatchMapping("/reviews/{reviewId}")
 public ResponseEntity<ApiResponse<ReviewResponse>> updateMyReview(
         @PathVariable Long reviewId,
         @Valid @RequestBody ReviewUpdateRequest request){
   return ResponseEntity.ok(ApiResponse.success("후기 수정 성공", mypageActivityService.updateMyReview(reviewId, request)));
 }


 //내 후기 삭제
    @DeleteMapping("/reviews/{reviewId}")
 public ResponseEntity<ApiResponse<Void>> deleteMyReview(@PathVariable Long reviewId){
        mypageActivityService.deleteMyReview(reviewId);
        return ResponseEntity.ok(ApiResponse.success("후기 삭제 성공", null));
 }
}
