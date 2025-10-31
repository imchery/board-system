package study.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 이메일 인증 코드 발송 요청
 */
@Getter
@NoArgsConstructor
public class EmailVerificationRequest {

    /**
     * 이메일 주소
     */
    private String email;
}
