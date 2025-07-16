package study.common.lib.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import study.common.lib.response.ApiResponse;

/**
 * 전역 예외 처리 핸들러
 * 모든 서비스에서 발생하는 예외를 일관성 있게 처리
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * BaseException 처리
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<Void>> handleBaseException(BaseException e) {
        log.error("BaseException occurred: {}", e.getMessage(), e);

        ApiResponse<Void> response = ApiResponse.error(e.getMessage(), e.getErrorCode()
                .getCode());
        return ResponseEntity
                .status(e.getErrorCode()
                        .getHttpStatus())
                .body(response);
    }

    /**
     * Validation 예외 처리 (@Valid 실패)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        log.error("Validation failed: {}", e.getMessage());

        String errorMessage = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((first, second) -> first + ", " + second)
                .orElse("입력값이 올바르지 않습니다.");

        ApiResponse<Void> response = ApiResponse.error(errorMessage, ErrorCode.VALIDATION_FAILED.getCode());
        return ResponseEntity
                .status(ErrorCode.VALIDATION_FAILED.getHttpStatus())
                .body(response);
    }

    /**
     * 파라미터 타입 불일치 예외 처리
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.error("Parameter type mismatch: {}", e.getMessage());

        String errorMessage = String.format("파라미터 '%s'의 값이 올바르지 않습니다.", e.getName());
        ApiResponse<Void> response = ApiResponse.error(errorMessage, ErrorCode.INVALID_PARAMETER_TYPE.getCode());

        return ResponseEntity
                .status(ErrorCode.INVALID_PARAMETER_TYPE.getHttpStatus())
                .body(response);
    }

    /**
     * Constraint Violation 예외 처리
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(ConstraintViolationException e) {
        log.error("Constraint violation: {}", e.getMessage());

        ApiResponse<Void> response = ApiResponse.error("입력값 검증에 실패했습니다.", ErrorCode.VALIDATION_FAILED.getCode());
        return ResponseEntity
                .status(ErrorCode.VALIDATION_FAILED.getHttpStatus())
                .body(response);
    }

    /**
     * IllegalArgumentException 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("Illegal argument: {}", e.getMessage());

        ApiResponse<Void> response = ApiResponse.error(e.getMessage(), ErrorCode.INVALID_REQUEST.getCode());
        return ResponseEntity
                .status(ErrorCode.INVALID_REQUEST.getHttpStatus())
                .body(response);
    }

    /**
     * RuntimeException 처리
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException e) {
        log.error("Runtime exception occurred: {}", e.getMessage(), e);

        ApiResponse<Void> response = ApiResponse.error("처리 중 오류가 발생했습니다.", ErrorCode.INTERNAL_SERVER_ERROR.getCode());
        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(response);
    }

    /**
     * 모든 예외 처리 (최종 fallback)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Unexpected exception occurred: {}", e.getMessage(), e);

        ApiResponse<Void> response = ApiResponse.error("서버 내부 오류가 발생했습니다.", ErrorCode.INTERNAL_SERVER_ERROR.getCode());
        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(response);
    }
}
