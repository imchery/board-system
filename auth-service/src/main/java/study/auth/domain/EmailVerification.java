package study.auth.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "email_verifications")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailVerification {

    @Id
    private String id;

    /**
     * 이메일 주소
     */
    private String email;

    /**
     * 인증 코드 (6자리)
     */
    private String code;

    /**
     * 생성 시간
     */
    private LocalDateTime createdAt;

    /**
     * 만료 시간 (30분 후)
     * MongoDB TTL Index: expiresAt 시간이 되면 자동 삭제
     */
    @Indexed(name = "expiresAt_ttl", expireAfter = "0s")
    private LocalDateTime expiresAt;

    /**
     * 인증 완료 여부
     */
    private boolean verified;

    // ========== 인증 관련 함수 ==========

    /**
     * 인증 코드 검증
     *
     * @param inputCode 입력한 코드
     * @return 코드일치여부
     */
    public boolean isValid(String inputCode) {
        // 1. 이미 인증됨
        if (this.verified) {
            return false;
        }

        // 2. 만료됨
        if (LocalDateTime.now()
                .isAfter(this.expiresAt)) {
            return false;
        }

        // 3. 코드 일치
        return this.code.equals(inputCode);
    }

    /**
     * 인증 완료 처리
     */
    public void verify() {
        this.verified = true;
    }

    /**
     * 만료 여부 확인
     *
     * @return 만료 여부
     */
    public boolean isExpired() {
        return LocalDateTime.now()
                .isAfter(this.expiresAt);
    }
}
