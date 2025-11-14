package study.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordResponse {

    private String email; // 임시 비밀번호를 발송할 이메일

    public static ResetPasswordResponse of(String email) {
        return ResetPasswordResponse.builder()
                .email(email)
                .build();
    }

    /**
     * 이메일 마스킹 처리
     * 예: user@gmail.com -> u***@gmail.com
     *
     * @param email 이메일
     * @return 마스킹 처리된 이메일
     */
    private static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }

        String[] parts = email.split("@");
        String localPart = parts[0];
        String domainPart = parts[1];

        return localPart.charAt(0) + "** * @" + domainPart;
    }
}
