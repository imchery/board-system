package study.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import study.auth.dto.*;
import study.auth.service.AuthService;
import study.auth.service.EmailService;
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
    private final EmailService emailService;

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
     * 회원가입
     * 새로운 사용자 계정 생성
     *
     * @param request 회원가입 요청(username, password, email, nickname)
     * @return 회원가입 완료 정보
     */
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseVO<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        log.debug("회원가입 API 호출 - username: {}", request.getUsername());

        return ResponseVO.ok(userService.signup(request));
    }

    /**
     * 아이디 중복 체크
     * 회원가입 시 아이디 사용 가능 여부 확인
     *
     * @param username 확인할 아이디
     * @return 사용 가능 여부(true: 사용가능, false: 중복)
     */
    @GetMapping("/check/username")
    public ResponseVO<Boolean> checkUsername(@RequestParam String username) {
        boolean available = userService.checkUsernameDuplicate(username);
        String message = available ? "사용 가능한 아이디입니다" : "이미 사용 중인 아이디입니다";
        return ResponseVO.ok(message, available);
    }

    /**
     * 이메일 중복 체크
     * 회원가입 시 이메일 사용 가능 여부 확인
     *
     * @param email
     * @return
     */
    @GetMapping("/check/email")
    public ResponseVO<Boolean> checkEmail(@RequestParam String email) {
        boolean available = userService.checkEmailDuplicate(email);
        String message = available ? "사용 가능한 이메일입니다" : "이미 사용 중인 이메일입니다";
        return ResponseVO.ok(message, available);
    }

    /**
     * 이메일 인증 코드 발송
     *
     * @param request
     * @return
     */
    @PostMapping("/email/send")
    public ResponseVO<Void> sendVerificationCode(@RequestBody EmailVerificationRequest request) {

        log.info("인증 코드 발송 요청 - email: {}", request.getEmail());

        emailService.sendVerificationCode(request.getEmail());
        return ResponseVO.ok("인증 코드가 발송되었습니다.", null);
    }

    /**
     * 이메일 인증 코드 확인
     * @param request
     * @return
     */
    @PostMapping("/email/verify")
    public ResponseVO<Boolean> verifyCode(@RequestBody EmailVerificationCheckRequest request) {
        log.info("인증 코드 확인 요청 - email: {}", request.getEmail());

        boolean verified = emailService.verifyCode(request.getEmail(), request.getCode());
        return ResponseVO.ok("이메일 인증이 완료되었습니다.", verified);
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
