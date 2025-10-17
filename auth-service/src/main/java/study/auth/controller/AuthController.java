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

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    /**
     * 로그인 API
     *
     * @param request
     * @return
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for username: {}", request.getUsername());

        LoginResponse loginResponse = authService.login(request);

        log.info("Login success - username: {}", request.getUsername());
        return ResponseEntity.ok(loginResponse);
    }

    /**
     * 토큰 검증
     *
     * @param authHeader
     * @return
     */
    @GetMapping("/validate")
    public ResponseEntity<Boolean> validateToken(@RequestHeader("Authorization") String authHeader) {
        log.debug("Token validation request");

        // "Bearer " 접두사 제거
        String token = extractToken(authHeader);
        boolean isValid = authService.validateToken(token);

        log.debug("Token validation result: {}", isValid);
        return ResponseEntity.ok(isValid);

    }

    /**
     * 토큰에서 사용자 정보 추출 API
     *
     * @param authHeader
     * @return
     */
    @GetMapping("/user")
    public ResponseEntity<String> getUserFromToken(@RequestHeader("Authorization") String authHeader) {
        log.debug("Get user from token request");

        String token = extractToken(authHeader);
        String username = authService.getUsernameFromToken(token);

        log.debug("User extracted from token: {}", username);
        return ResponseEntity.ok(username);

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
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth Service is running");
    }

    /**
     * Authorization 헤더에서 토큰 추출
     *
     * @param authHeader Authorization 헤더
     * @return 순수 토큰 문자열
     */
    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("유효하지 않은 Authorization 헤더입니다.");
        }
        return authHeader.substring(7); // "Bearer " 제거
    }
}
