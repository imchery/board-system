package study.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import study.auth.dto.LoginRequest;
import study.auth.dto.LoginResponse;
import study.common.lib.config.JwtTokenService;
import study.common.lib.exception.BaseException;
import study.common.lib.exception.ErrorCode;

/**
 * 인증 비즈니스 로직 처리 Service
 * 로그인, 토큰 검증, 사용자 정보 추출 등의 로직 담당
 */
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
     * 사용자명과 비밀번호 검증 후 JWT 토큰 발급
     * TODO: 향후 user-service와 연동하여 DB 조회로 변경
     *
     * @param request 로그인 요청
     * @return 로그인 응답(토큰+사용자명)
     */
    public LoginResponse login(LoginRequest request) {
        log.info("로그인 처리 시작 - username: {}", request.getUsername());

        // 사용자 인증
        if (!isValidUser(request.getUsername(), request.getPassword())) {
            log.warn("로그인 실패 - 잘못된 인증 정보: username={}", request.getUsername());
            throw new BaseException(
                    ErrorCode.INVALID_CREDENTIALS, "아이디 또는 비밀번호가 틀렸습니다."
            );
        }

        // JWT 토큰 생성
        String token = jwtTokenService.generateToken(request.getUsername());
        log.info("JWT 토큰 생성 완료 - username: {}", request.getUsername());
        return LoginResponse.success(token, request.getUsername());
    }

    /**
     * 사용자 인증
     * 현재는 application.properties의 값과 비교
     * TODO: 향후 user-service와 연동하여 DB 조회로 변경
     *
     * @param username 사용자명
     * @param password 비밀번호
     * @return 인증 성공 여부
     */
    private boolean isValidUser(String username, String password) {
        return adminUsername.equals(username) && adminPassword.equals(password);
    }

    /**
     * 토큰 유효성 및 만료 확인
     *
     * @param token JWT 토큰
     * @return 유효성(true,
     */
    public boolean validateToken(String token) {
        return jwtTokenService.validateToken(token) && !jwtTokenService.isTokenExpired(token);
    }

    /**
     * 토큰에서 사용자명 추출
     *
     * @param token token JWT 토큰
     * @return 사용자명
     */
    public String getUsernameFromToken(String token) {
        if (!validateToken(token)) {
            log.warn("유효하지 않은 토큰으로 사용자 정보 추출 시도");
            throw new BaseException(
                    ErrorCode.INVALID_TOKEN,
                    "유효하지 않거나 만료된 토큰입니다."
            );
        }
        throw new RuntimeException("Invalid or expired token");
    }
}
