package com.grimgate.grimgate_backend.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

// 서비스 전반에서 사용하는 에러 코드 목록
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 인증/계정
    ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    NICKNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
    TERMS_NOT_AGREED(HttpStatus.BAD_REQUEST, "서비스 이용약관에 동의해야 합니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 올바르지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    // 토큰
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    REVOKED_TOKEN(HttpStatus.UNAUTHORIZED, "이미 로그아웃된 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "리프레시 토큰을 찾을 수 없습니다."),

    // 회원/역할
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "멤버 정보를 찾을 수 없습니다."),
    MANAGER_NOT_FOUND(HttpStatus.NOT_FOUND, "매니저 정보를 찾을 수 없습니다."),
    INVALID_ROLE(HttpStatus.BAD_REQUEST, "유효하지 않은 역할입니다."),

    // 프로필/마이페이지
    PROFILE_CHARACTER_NOT_FOUND(HttpStatus.NOT_FOUND, "프로필 캐릭터를 찾을 수 없습니다."),
    TITLE_NOT_FOUND(HttpStatus.NOT_FOUND, "칭호를 찾을 수 없습니다."),

    //마이페이지 후기
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "예약을 찾을 수 없습니다."),
    RESERVATION_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "완료된 예약만 후기 작성이 가능합니다."),
    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 후기를 작성한 예약입니다."),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "후기를 찾을 수 없습니다."),
    REVIEW_NOT_OWNER(HttpStatus.FORBIDDEN, "본인의 후기만 수정/삭제할 수 있습니다."),
    IMAGE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "이미지는 최대 3장까지 등록 가능합니다."),

    // 지점/테마 (사장님 페이지)
    BRANCH_NOT_FOUND(HttpStatus.NOT_FOUND, "지점을 찾을 수 없습니다."),
    BRANCH_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 지점에 대한 권한이 없습니다."),
    THEME_NOT_FOUND(HttpStatus.NOT_FOUND, "테마를 찾을 수 없습니다."),

    // 메이트 모집
    MATE_POST_NOT_FOUND(HttpStatus.NOT_FOUND, "메이트 모집글을 찾을 수 없습니다."),
    MATE_POST_FORBIDDEN(HttpStatus.FORBIDDEN, "다른 사용자의 메이트 모집글입니다."),
    MATE_POST_INVALID_DEADLINE(HttpStatus.BAD_REQUEST, "마감일은 모임 시간보다 늦을 수 없습니다."),
    MATE_POST_INVALID_MEETING_TIME(HttpStatus.BAD_REQUEST, "모임 시간은 현재 이후여야 합니다."),
    MATE_POST_INVALID_OPEN_CHAT_URL(HttpStatus.BAD_REQUEST, "카카오 오픈채팅 URL 형식이 올바르지 않습니다."),
    INVALID_THEME_CAPACITY(HttpStatus.BAD_REQUEST, "최소 인원은 최대 인원보다 클 수 없습니다."),

    // 결제
    INVALID_RESERVATION_STATUS(HttpStatus.BAD_REQUEST, "결제 가능한 예약 상태가 아닙니다."),
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "결제 요청 금액이 예약 금액과 일치하지 않습니다."),
    PAYMENT_ALREADY_EXISTS(HttpStatus.CONFLICT, "해당 예약에 대한 결제 내역이 이미 존재합니다.");

    private final HttpStatus httpStatus;
    private final String message;
}