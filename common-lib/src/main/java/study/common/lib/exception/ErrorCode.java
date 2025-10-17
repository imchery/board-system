package study.common.lib.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 시스템 전체에서 사용하는 에러 코드 정의
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // ===== 공통 에러 =====
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C001", "서버 내부 오류가 발생했습니다"),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "C002", "잘못된 요청입니다"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "C003", "인증이 필요합니다"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "C004", "권한이 없습니다"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "C005", "요청한 리소스를 찾을 수 없습니다"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C006", "허용되지 않은 HTTP 메서드입니다"),

    // ===== 인증/인가 관련 =====
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A001", "유효하지 않은 토큰입니다"),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "A002", "만료된 토큰입니다"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "A003", "아이디 또는 비밀번호가 틀렸습니다"),
    INVALID_TOKEN_TYPE(HttpStatus.UNAUTHORIZED, "A004", "잘못된 토큰 타입입니다"),
    MISSING_TOKEN(HttpStatus.UNAUTHORIZED, "A005", "토큰이 없습니다"),
    INVALID_TOKEN_FORMAT(HttpStatus.UNAUTHORIZED, "A006", "토큰 형식이 올바르지 않습니다"),

    // ===== 사용자 관련 =====
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다"),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "U002", "이미 존재하는 사용자입니다"),
    USER_DISABLED(HttpStatus.FORBIDDEN, "U003", "비활성화된 사용자입니다"),
    USER_ACCESS_DENIED(HttpStatus.FORBIDDEN, "U004", "사용자 접근이 거부되었습니다"),

    DUPLICATE_USERNAME(HttpStatus.CONFLICT, "U005", "이미 사용 중인 아이디입니다"),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "U006", "이미 사용 중인 이메일입니다"),
    SIGNUP_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "U007", "회원가입에 실패했습니다"),

    // ===== 게시글 관련 =====
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "게시글을 찾을 수 없습니다"),
    POST_ACCESS_DENIED(HttpStatus.FORBIDDEN, "P002", "게시글에 대한 권한이 없습니다"),
    POST_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "P003", "이미 삭제된 게시글입니다"),
    POST_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "P004", "게시글 생성에 실패했습니다"),
    POST_UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "P005", "게시글 수정에 실패했습니다"),

    // ===== 댓글 관련 =====
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "R001", "댓글을 찾을 수 없습니다"),
    COMMENT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "R002", "댓글에 대한 권한이 없습니다"),
    COMMENT_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "R003", "이미 삭제된 댓글입니다"),

    // ===== 검증 관련 =====
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "V001", "입력값이 올바르지 않습니다"),
    MISSING_REQUIRED_FIELD(HttpStatus.BAD_REQUEST, "V002", "필수 입력값이 누락되었습니다"),
    INVALID_PARAMETER_TYPE(HttpStatus.BAD_REQUEST, "V003", "파라미터 타입이 올바르지 않습니다"),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "V004", "데이터 검증에 실패했습니다"),

    // ===== 파일 관련 =====
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "F001", "파일을 찾을 수 없습니다"),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "F002", "파일 업로드에 실패했습니다"),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "F003", "지원하지 않는 파일 형식입니다"),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "F004", "파일 크기가 제한을 초과했습니다");


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
