package study.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 아이디 찾기 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FindUsernameRequest {

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    @NotBlank(message = "인증 코드는 필수입니다")
    private String verificationCode;
}
