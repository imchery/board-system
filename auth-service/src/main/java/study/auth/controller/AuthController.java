package study.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import study.auth.dto.LoginRequest;
import study.auth.dto.LoginResponse;
import study.auth.service.AuthService;
import study.auth.service.UserService;
import study.common.lib.exception.BaseException;
import study.common.lib.exception.ErrorCode;
import study.common.lib.response.ResponseVO;

/**
 * 인증 관련 API Controller
 * 로그인, 토큰 검증, 사용자 정보 추출 등의 인증 기능 제공
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    /**
     * 로그인
     * 사용자명과 비밀번호로 인증 후 JWT 토큰 발급
     *
     * @param request 로그인 요청(username, password)
     * @return JWT 토큰 및 사용자 정보
     */
    @PostMapping("/login")
    public ResponseVO<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("로그인 시도 - username: {}", request.getUsername());

        LoginResponse loginResponse = authService.login(request);

        log.info("로그인 성공 - username: {}", request.getUsername());
        return ResponseVO.ok(loginResponse);
    }

    /**
     * 토큰 유효성 검증
     * JWT 토큰의 유효성과 만료 여부를 확인
     *
     * @param authHeader Authorization 헤더(Bearer {token})
     * @return 토큰 유효성(true: 유효, false: 무효)
     */
    @GetMapping("/validate")
    public ResponseVO<Boolean> validateToken(@RequestHeader("Authorization") String authHeader) {
        log.debug("토큰 검증 요청");

        // "Bearer " 접두사 제거
        String token = extractToken(authHeader);
        boolean isValid = authService.validateToken(token);

        log.debug("토큰 검증 결과: {}", isValid);
        return ResponseVO.ok(isValid);

    }

    /**
     * 토큰에서 사용자 정보 추출
     * JWT 토큰에서 사용자명을 추출
     *
     * @param authHeader Authorization 헤더(Bearer {token})
     * @return 사용자명
     */
    @GetMapping("/user")
    public ResponseVO<String> getUserFromToken(@RequestHeader("Authorization") String authHeader) {
        log.debug("토큰에서 사용자 정보 추출 요청");

        String token = extractToken(authHeader);
        String username = authService.getUsernameFromToken(token);

        log.debug("추출된 사용자명: {}", username);
        return ResponseVO.ok(username);

    }

//    @PostMapping("/signup")
//    @ResponseStatus(HttpStatus.CREATED)
//    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
//
//    }


    /**
     * 서비스 상태 확인 API(헬스체크)
     *
     * @return
     */
    @GetMapping("/health")
    public ResponseVO<String> health() {
        return ResponseVO.ok("Auth Service is running");
    }

    /**
     * Authorization 헤더에서 토큰 추출
     *
     * @param authHeader Authorization 헤더
     * @return 순수 토큰 문자열
     */
    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("유효하지 않은 Authorization 헤더");
            throw new BaseException(ErrorCode.UNAUTHORIZED, "유효하지 않은 Authorization 헤더입니다.");
        }
        return authHeader.substring(7); // "Bearer " 제거
    }
}
