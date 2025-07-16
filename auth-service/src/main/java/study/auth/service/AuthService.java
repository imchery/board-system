package study.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import study.auth.dto.LoginRequest;
import study.auth.dto.LoginResponse;
import study.common.lib.config.JwtTokenService;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenService jwtTokenService;

    @Value("${auth.admin.username}")
    private String adminUsername;

    @Value("${auth.admin.password}")
    private String adminPassword;

    /**
     * 로그인 처리
     *
     * @param request
     * @return
     */
    public LoginResponse login(LoginRequest request) {
        if (isValidUser(request.getUsername(), request.getPassword())) {
            String token = jwtTokenService.generateToken(request.getUsername());
            log.info("JWT token generated for user: {}", request.getUsername());
            return LoginResponse.success(token, request.getUsername());
        } else {
            log.warn("Invalid credentials for user: {}", request.getUsername());
            throw new RuntimeException("잘못된 사용자명 또는 비밀번호입니다");
        }
    }

    /**
     * 사용자 인증
     * 현재는 application.properties의 값과 비교
     * 추후 user-service와 연동
     *
     * @param username
     * @param password
     * @return
     */
    private boolean isValidUser(String username, String password) {
        return adminUsername.equals(username) && adminPassword.equals(password);
    }

    /**
     * 토큰 유효성 및 만료 확인
     *
     * @param token
     * @return
     */
    public boolean validateToken(String token) {
        return jwtTokenService.validateToken(token) && !jwtTokenService.isTokenExpired(token);
    }

    /**
     * 토큰에서 사용자명 추출
     *
     * @param token
     * @return
     */
    public String getUsernameFromToken(String token) {
        if (validateToken(token)) {
            return jwtTokenService.getUsernameFromToken(token);
        }
        throw new RuntimeException("Invalid or expired token");
    }
}
