package study.common.lib.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import study.common.lib.response.ResponseVO;

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
    public ResponseEntity<ResponseVO> handleBaseException(BaseException e) {
        log.error("BaseException occurred: {}", e.getMessage(), e);

        ResponseVO response = ResponseVO.error(e.getMessage());
        return ResponseEntity
                .status(e.getErrorCode()
                        .getHttpStatus())
                .body(response);
    }

    /**
     * Validation 예외 처리 (@Valid 실패)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseVO> handleValidationException(MethodArgumentNotValidException e) {
        log.error("Validation failed: {}", e.getMessage());

        String errorMessage = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((first, second) -> first + ", " + second)
                .orElse("입력값이 올바르지 않습니다.");

        ResponseVO response = ResponseVO.error(errorMessage);
        return ResponseEntity
                .status(ErrorCode.VALIDATION_FAILED.getHttpStatus())
                .body(response);
    }

    /**
     * 파라미터 타입 불일치 예외 처리
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ResponseVO> handleTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.error("Parameter type mismatch: {}", e.getMessage());

        String errorMessage = String.format("파라미터 '%s'의 값이 올바르지 않습니다.", e.getName());
        ResponseVO response = ResponseVO.error(errorMessage);

        return ResponseEntity
                .status(ErrorCode.INVALID_PARAMETER_TYPE.getHttpStatus())
                .body(response);
    }

    /**
     * Constraint Violation 예외 처리
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ResponseVO> handleConstraintViolationException(ConstraintViolationException e) {
        log.error("Constraint violation: {}", e.getMessage());

        ResponseVO response = ResponseVO.error("입력값 검증에 실패했습니다.");
        return ResponseEntity
                .status(ErrorCode.VALIDATION_FAILED.getHttpStatus())
                .body(response);
    }

    /**
     * IllegalArgumentException 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseVO> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("Illegal argument: {}", e.getMessage());

        ResponseVO response = ResponseVO.error(e.getMessage());
        return ResponseEntity
                .status(ErrorCode.INVALID_REQUEST.getHttpStatus())
                .body(response);
    }

    /**
     * RuntimeException 처리
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ResponseVO> handleRuntimeException(RuntimeException e) {
        log.error("Runtime exception occurred: {}", e.getMessage(), e);

        ResponseVO response = ResponseVO.error("처리 중 오류가 발생했습니다.");
        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(response);
    }

    /**
     * 모든 예외 처리 (최종 fallback)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseVO> handleException(Exception e) {
        log.error("Unexpected exception occurred: {}", e.getMessage(), e);

        ResponseVO response = ResponseVO.error("서버 내부 오류가 발생했습니다.");
        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(response);
    }
}
