package study.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 아이디 찾기 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FindUsernameResponse {

    private String username;
    private String maskedUsername;

    public static String maskUsername(String username) {
        if (username == null || username.length() < 4) {
            return username;
        }

        if (username.length() <= 6) {
            return username.substring(0, 2) + "***";
        } else {
            return username.substring(0, 3) + "***";
        }
    }

}
