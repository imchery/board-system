package study.common.lib.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

/**
 * 통합 응답 객체
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseVO {

    private boolean result;
    private String message;
    private Object data;
    private long timestamp;

    // ======================= 생성자 =======================
    private ResponseVO() {
        this.timestamp = System.currentTimeMillis();
    }

    private ResponseVO(boolean result, String message) {
        this();
        this.result = result;
        this.message = message;
    }

    private ResponseVO(boolean result, String message, Object data) {
        this(result, message);
        this.data = data;
    }

    // ======================= 정적 팩토리 메서드 =======================

    public static ResponseVO ok() {
        return new ResponseVO(true, "요청이 성공적으로 처리되었습니다.");
    }

    public static ResponseVO ok(Object data) {
        return new ResponseVO(true, "요청이 성공적으로 처리되었습니다.", data);
    }

    public static ResponseVO ok(String message, Object data) {
        return new ResponseVO(true, message, data);
    }

    public static ResponseVO saveOk() {
        return new ResponseVO(true, "저장되었습니다.");
    }

    public static ResponseVO saveOk(Object data) {
        return new ResponseVO(true, "저장되었습니다.", data);
    }

    public static ResponseVO updateOk() {
        return new ResponseVO(true, "수정되었습니다.");
    }

    public static ResponseVO updateOk(Object data) {
        return new ResponseVO(true, "수정되었습니다.", data);
    }

    public static ResponseVO deleteOk() {
        return new ResponseVO(true, "삭제되었습니다.");
    }

    public static ResponseVO error(String message) {
        return new ResponseVO(false, message);
    }

    public static ResponseVO authFail() {
        return new ResponseVO(false, "인증이 필요합니다.");
    }

    public static ResponseVO accessDenied() {
        return new ResponseVO(false, "권한이 없습니다.");
    }

    public static ResponseVO noData() {
        return new ResponseVO(false, "데이터를 찾을 수 없습니다.");
    }

}
