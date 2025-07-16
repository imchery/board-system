package study.common.lib.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 모든 커스텀 예외의 기본 클래스
 * ErrorCode를 포함하여 통일된 예외 처리
 */
@Getter
@RequiredArgsConstructor
public class BaseException extends RuntimeException {

    private final ErrorCode errorCode;

    public BaseException(Exception errorCode) {
        super(errorCode.getMessage());
        this.errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
    }

    public BaseException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BaseException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public BaseException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

}
