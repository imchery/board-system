package study.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 이메일 설정 Properties
 * application.yml의 email.verification 설정을 자동 매핑
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "email.verification")
public class EmailProperties {

    /**
     * 인증 코드 길이 (기본값: 6)
     * yml: code-length -> codeLength 자동 변환
     */
    private int codeLength = 6;

    /**
     * 만료 시간 (분) (기본값: 30)
     * yml expiration-minutes -> expirationMinutes 자동 변환
     */
    private int expirationMinutes = 30;

    /**
     * 발신자 이름 (기본값: Board System)
     * yml: from-name -> formName 자동 변환
     */
    private String fromName = "Board System";

    /**
     * 발신자 이메일
     * yml: from-email -> fromEmail 자동 변환
     */
    private String fromEmail;

}
