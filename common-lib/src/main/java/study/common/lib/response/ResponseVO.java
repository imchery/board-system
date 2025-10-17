package study.common.lib.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import org.springframework.validation.FieldError;

import java.util.List;

/**
 * 통합 응답 객체
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseVO<T> {

    /**
     * 요청 성공 여부
     */
    private final boolean result;

    /**
     * 응답 메시지
     */
    private final String message;

    /**
     * 응답 데이터 (제네릭 타입)
     */
    private final T data;

    /**
     * 필드 검증 에러 목록 (Validation 실패 시)
     */
    private final List<FieldErrorDetail> fieldErrors;

    /**
     * 응답 생성 시간
     */
    private final long timestamp;

    // ======================= 생성자 =======================

    /**
     * 내부 생성자
     *
     * @param result
     * @param message
     * @param data
     * @param fieldErrors
     */
    private ResponseVO(boolean result, String message, T data, List<FieldErrorDetail> fieldErrors) {
        this.result = result;
        this.message = message;
        this.data = data;
        this.fieldErrors = fieldErrors;
        this.timestamp = System.currentTimeMillis();
    }

    // ======================= 성공 응답 =======================

    /**
     * 데이터 없는 성공 응답
     * 예시: 삭제 완료, 업데이트 완료 등 반환 데이터가 없는 경우
     *
     * @return 성공 응답(data = null)
     */
    public static ResponseVO<Void> ok() {
        return new ResponseVO<>(true, "요청이 성공적으로 처리되었습니다.", null, null);
    }

    /**
     * 데이터와 함께 성공 응답
     * 예시: 조회 성공, 생성 성공 등 데이터를 반환하는 경우
     *
     * @param <T>  데이터 타입
     * @param data 응답 데이터
     * @return 성공 응답
     */
    public static <T> ResponseVO<T> ok(T data) {
        return new ResponseVO<>(true, "요청이 성공적으로 처리되었습니다.", data, null);
    }

    /**
     * 커스텀 메시지와 데이터로 성공 응답
     *
     * @param message 커스텀 메시지
     * @param data    응답 데이터
     * @param <T>     데이터 타입
     * @return 성공 응답
     */
    public static <T> ResponseVO<T> ok(String message, T data) {
        return new ResponseVO<>(true, message, data, null);
    }

    /**
     * 저장 성공 (데이터 없음)
     *
     * @return 저장 성공 응답
     */
    public static ResponseVO<Void> saveOk() {
        return new ResponseVO<>(true, "저장되었습니다.", null, null);
    }

    /**
     * 저장 성공 (저장된 데이터 반환)
     *
     * @param data 저장된 데이터
     * @param <T>  데이터 타입
     * @return 저장 성공 응답
     */
    public static <T> ResponseVO<T> saveOk(T data) {
        return new ResponseVO<>(true, "저장되었습니다.", data, null);
    }

    /**
     * 수정 성공 (데이터 없음)
     *
     * @return 수정 성공 응답
     */
    public static ResponseVO<Void> updateOk() {
        return new ResponseVO<>(true, "수정되었습니다.", null, null);
    }

    /**
     * 수정 성공 (수정된 데이터 반환)
     *
     * @param data 수정된 데이터
     * @param <T>  데이터 타입
     * @return 수정 성공 응답
     */
    public static <T> ResponseVO<T> updateOk(T data) {
        return new ResponseVO<>(true, "수정되었습니다.", data, null);
    }

    /**
     * 삭제 성공
     *
     * @return 삭제 성공 응답
     */
    public static ResponseVO<Void> deleteOk() {
        return new ResponseVO<>(true, "삭제되었습니다.", null, null);
    }

    // ======================= 실패 응답 =======================

    /**
     * 에러 응답
     *
     * @param message 에러 메시지
     * @return 에러 응답
     */
    public static ResponseVO<Void> error(String message) {
        return new ResponseVO<>(false, message, null, null);
    }

    /**
     * Validation 실패 응답
     * Spring Validation 실패 시 사용
     * GlobalExceptionHandler에서 MethodArgumentNotValidException 처리 시 호출
     *
     * @param errors Spring의 FieldError 목록
     * @return Validation 실패 응답
     */
    public static ResponseVO<Void> validationError(List<FieldError> errors) {
        List<FieldErrorDetail> fieldErrors = errors.stream()
                .map(FieldErrorDetail::from)
                .toList();
        return new ResponseVO<>(false, "입력값 검증에 실패했습니다.", null, fieldErrors);
    }

    /**
     * 인증 실패 응답
     *
     * @return 인증 실패 응답
     */
    public static ResponseVO<Void> authFail() {
        return new ResponseVO<>(false, "인증이 필요합니다.", null, null);
    }

    /**
     * 권한 없음 응답
     *
     * @return 권한 없음 응답
     */
    public static ResponseVO<Void> accessDenied() {
        return new ResponseVO<>(false, "권한이 없습니다.", null, null);
    }

    /**
     * 데이터 없음 응답
     *
     * @return 데이터 없음 응답
     */
    public static ResponseVO<Void> noData() {
        return new ResponseVO<>(false, "데이터를 찾을 수 없습니다.", null, null);
    }

    // ======================= 내부 클래스 =======================

    /**
     * 필드 에러 상세 정보
     * 프론트엔드에서 필드별로 에러 메시지를 표시할 수 있도록 제공
     */
    @Getter
    public static class FieldErrorDetail {

        /**
         * 에러가 발생한 필드명
         */
        private final String field;

        /**
         * 에러 메세지
         */
        private final String message;

        /**
         * 거부된 입력값
         */
        private final Object rejectedValue;

        public FieldErrorDetail(String field, String message, Object rejectedValue) {
            this.field = field;
            this.message = message;
            this.rejectedValue = rejectedValue;
        }

        /**
         * Spring의 FieldError를 FieldErrorDetail로 변환
         *
         * @param error Spring FieldError
         * @return FieldErrorDetail
         */
        public static FieldErrorDetail from(FieldError error) {
            return new FieldErrorDetail(
                    error.getField(),
                    error.getDefaultMessage(),
                    error.getRejectedValue()
            );
        }
    }

}
