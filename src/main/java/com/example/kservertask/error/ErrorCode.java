package com.example.kservertask.error;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    PAYMENT_REJECTED(HttpStatus.CONFLICT, "결제가 거부되었습니다."),
    ORDER_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "주문에 실패했습니다."),
    DUPLICATE_REQUEST(HttpStatus.CONFLICT, "중복된 요청입니다."),
    POINT_CHARGE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "충전에 실패했습니다."),
    MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "조회된 메뉴가 없습니다."),
    MENU_CATEGORY_MISSING(HttpStatus.INTERNAL_SERVER_ERROR, "카테고리가 누락되었습니다."),
    USER_INFORMATION_MISMATCH(HttpStatus.BAD_REQUEST, "회원 정보가 일치하지 않습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
