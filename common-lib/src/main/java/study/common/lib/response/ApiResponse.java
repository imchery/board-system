package study.common.lib.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

/**
 * API 응답을 위한 공통 래퍼 클래스
 * 모든 서비스에서 일관된 응답 형태 제공
 *
 * @param <T>
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * 성공여부
     */
    private boolean success;

    /**
     * 응답 메세지
     */
    private String message;

    /**
     * 응답 데이터
     */
    private T data;

    /**
     * 에러 코드(실패 시)
     */
    private String errorCode;

    /**
     * 타입스탬프
     */
    private long timestamp;

    /**
     * 성공 응답 생성(데이터만)
     *
     * @param data
     * @param <T>
     * @return
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("요청이 성공적으로 처리되었습니다.")
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 성공 응답 생성(데이터 + 메세지)
     *
     * @param data
     * @param message
     * @return
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 성공 응답 생성(메세지만)
     *
     * @param message
     * @return
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 실패 응답 생성(메세지만)
     *
     * @param message
     * @param <T>
     * @return
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 실패 응답 생성(메세지 + 에러코드)
     *
     * @param message
     * @param <T>
     * @return
     */
    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .timestamp(System.currentTimeMillis())
                .build();
    }

}
