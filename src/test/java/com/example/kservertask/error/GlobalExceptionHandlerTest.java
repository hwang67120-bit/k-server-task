package com.example.kservertask.error;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void returnsConfiguredBusinessError() {
        BusinessException exception = new BusinessException(ErrorCode.DUPLICATE_REQUEST);

        ResponseEntity<ErrorResponse> response = handler.handleBusinessException(exception);

        assertThat(response.getStatusCode()).isEqualTo(ErrorCode.DUPLICATE_REQUEST.getStatus());
        assertThat(response.getBody()).isEqualTo(
                new ErrorResponse("DUPLICATE_REQUEST", "중복된 요청입니다.")
        );
    }
}
