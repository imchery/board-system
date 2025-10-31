package study.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 이메일 인증 코드 확인 요청
 */
@Getter
@NoArgsConstructor
public class EmailVerificationCheckRequest {

    /**
     * 이메일 주소
     */
    private String email;

    /**
     * 인증 코드
     */
    private String code;

}
