package study.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import study.auth.dto.LoginRequest;
import study.auth.dto.LoginResponse;
import study.auth.service.AuthService;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 로그인 API
     *
     * @param request
     * @return
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for username: {}", request.getUsername());

        try {
            LoginResponse loginResponse = authService.login(request);
            return ResponseEntity.ok(loginResponse);

        } catch (Exception e) {
            log.error("Login failed for username: {}", request.getUsername(), e);
            return ResponseEntity.badRequest()
                    .body(LoginResponse.failure("로그인 실패: " + e.getMessage()));
        }
    }

    /**
     * 토큰 검증
     *
     * @param authHeader
     * @return
     */
    @GetMapping("/validate")
    public ResponseEntity<Boolean> validateToken(@RequestHeader("Authorization") String authHeader) {
        log.info("Token validation request");

        try {
            // "Bearer " 접두사 제거
            String token = authHeader.replace("Bearer ", "");
            boolean isValid = authService.validateToken(token);

            log.info("Token validation result: {}", isValid);
            return ResponseEntity.ok(isValid);
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return ResponseEntity.ok(false);
        }
    }

    /**
     * 토큰에서 사용자 정보 추출 API
     *
     * @param authHeader
     * @return
     */
    @GetMapping("/user")
    public ResponseEntity<String> getUserFromToken(@RequestHeader("Authorization") String authHeader) {
        log.info("Get user from token request");

        try {
            String token = authHeader.replace("Bearer ", "");
            String username = authService.getUsernameFromToken(token);

            log.info("User extracted from token: {}", username);
            return ResponseEntity.ok(username);
        } catch (Exception e) {
            log.error("Failed to extract user from token: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body("Invalid token");
        }
    }

    /**
     * 서비스 상태 확인 API(헬스체크)
     *
     * @return
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth Service is running");
    }
}
