package study.common.lib.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import study.common.lib.response.ResponseVO;

/**
 * 전역 예외 처리 핸들러
 * 모든 서비스에서 발생하는 예외를 일관성 있게 처리하여
 * 통일된 에러 응답 형식 제공
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ======================= 비즈니스 예외 =======================

    /**
     * BaseException 처리(비즈니스 예외)
     *
     * @param e BaseException
     * @return 에러 응답
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ResponseVO<Void>> handleBaseException(BaseException e) {
        log.error("BaseException occurred: {}", e.getMessage(), e);

        ResponseVO<Void> response = ResponseVO.error(e.getMessage());
        return ResponseEntity
                .status(e.getErrorCode()
                        .getHttpStatus())
                .body(response);
    }

    // ======================= Validation 예외 =======================

    /**
     * Validation 예외 처리 (@Valid, @Validated 실패)
     * DTO의 Validation 어노테이션이 실패했을 때 발생
     *
     * @param e MethodArgumentNotValidException
     * @return Validation 에러 응답 (필드별 에러 포함)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseVO<Void>> handleValidationException(MethodArgumentNotValidException e) {
        log.warn("Validation failed: {} error(s)",
                e.getBindingResult()
                        .getFieldErrorCount());

        e.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        log.debug("Field error: field={}, rejectedValue={}, message={}",
                                error.getField(),
                                error.getRejectedValue(),
                                error.getDefaultMessage())
                );

        ResponseVO<Void> response = ResponseVO.validationError(
                e.getBindingResult()
                        .getFieldErrors()
        );

        return ResponseEntity
                .status(ErrorCode.VALIDATION_FAILED.getHttpStatus())
                .body(response);
    }

    /**
     * Constraint Violation 예외 처리
     * - @Validated를 클래스 레벨에 사용했을 때 발생
     * - 주로 @RequestParam, @PathVariable의 검증 실패 시
     *
     * @param e ConstraintViolationException
     * @return 에러 응답
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ResponseVO<Void>> handleConstraintViolationException(ConstraintViolationException e) {
        log.error("Constraint violation: {}", e.getMessage());

        String errorMessage = e.getConstraintViolations()
                .stream()
                .findFirst()
                .map(ConstraintViolation::getMessage)
                .orElse("입력값 검증에 실패했습니다.");

        ResponseVO<Void> response = ResponseVO.error(errorMessage);
        return ResponseEntity
                .status(ErrorCode.VALIDATION_FAILED.getHttpStatus())
                .body(response);

    }

    // ======================= Spring 내장 예외 =======================

    /**
     * 파라미터 타입 불일치 예외 처리
     * ex) /posts/abc (id가 숫자여야하는데 문자일 경우)
     *
     * @param e MethodArgumentTypeMismatchException
     * @return 에러응답
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ResponseVO<Void>> handleTypeMismatchException(MethodArgumentTypeMismatchException e) {

        log.warn("Parameter type mismatch: name={}, value={}, requiredType={}",
                e.getName(), e.getValue(), e.getRequiredType());

        String errorMessge = String.format(
                "파라미터 '%s'의 값이 올바르지 않습니다. (입력값: %s)",
                e.getName(),
                e.getValue()
        );

        ResponseVO<Void> response = ResponseVO.error(errorMessge);
        return ResponseEntity
                .status(ErrorCode.INVALID_PARAMETER_TYPE.getHttpStatus())
                .body(response);
    }

    /**
     * 필수 파라미터 누락 예외 처리
     *
     * @param e MissingServletRequestParameterException
     * @return 에러 응답
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ResponseVO<Void>> handleMissingParameterException(MissingServletRequestParameterException e) {
        log.warn("Missing parameter: name={}, type={}",
                e.getParameterName(), e.getParameterType());

        String errorMessage = String.format(
                "필수 파라미터 '%s'가 누락되었습니다.",
                e.getParameterName()
        );

        ResponseVO<Void> response = ResponseVO.error(errorMessage);
        return ResponseEntity
                .status(ErrorCode.INVALID_REQUEST.getHttpStatus())
                .body(response);
    }

    /**
     * JSON 파싱 실패 예외 처리
     * 잘못된 JSON 형식, 필드 타입 불일치
     *
     * @param e HttpMessageNotReadableException
     * @return 에러 응답
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ResponseVO<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("HTTP message not readable: {}", e.getMessage());

        ResponseVO<Void> response = ResponseVO.error(
                "요청 본문을 읽을 수 없습니다. JSON 형식을 확인해주세요."
        );

        return ResponseEntity
                .status(ErrorCode.INVALID_REQUEST.getHttpStatus())
                .body(response);
    }


    /**
     * IllegalArgumentException 처리
     * 주로 비즈니스 로직에서 잘못된 인자가 전달되었을 때
     *
     * @param e IllegalArgumentException
     * @return 에러 응답
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseVO<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("Illegal argument: {}", e.getMessage());

        ResponseVO<Void> response = ResponseVO.error(e.getMessage());
        return ResponseEntity
                .status(ErrorCode.INVALID_REQUEST.getHttpStatus())
                .body(response);
    }

    // ======================= 일반 예외 =======================

    /**
     * RuntimeException 처리
     * 예상하지 못한 런타임 에러
     * 운영 환경에서는 상세 에러 메시지를 숨기고 일반 메시지만 노출
     *
     * @param e RuntimeException
     * @return 에러 응답
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ResponseVO<Void>> handleRuntimeException(RuntimeException e) {
        log.error("Runtime exception occurred: {}", e.getMessage(), e);

        ResponseVO<Void> response = ResponseVO.error("처리 중 오류가 발생했습니다.");
        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(response);
    }

    /**
     * 모든 예외 처리 (최종 fallback)
     * 위에서 처리되지 않은 모든 예외를 여기서 처리
     * 예상하지 못한 에러가 발생해도 서버가 죽지 않도록 보호
     *
     * @param e Exception
     * @return 에러응답
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseVO<Void>> handleException(Exception e) {
        log.error("Unexpected exception occurred: {}", e.getMessage(), e);

        ResponseVO<Void> response = ResponseVO.error("서버 내부 오류가 발생했습니다.");
        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(response);
    }
}
