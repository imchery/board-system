package study.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private String username;
    private String message;

    public static LoginResponse success(String token, String username) {
        return new LoginResponse(token, username, "로그인 성공");
    }

    public static LoginResponse failure(String message) {
        return new LoginResponse(null, null, message);
    }
}
