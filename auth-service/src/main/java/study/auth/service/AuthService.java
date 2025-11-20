package study.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import study.auth.dto.*;
import study.auth.entity.User;
import study.auth.repository.UserRepository;
import study.common.lib.config.JwtTokenService;
import study.common.lib.exception.BaseException;
import study.common.lib.exception.ErrorCode;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 인증 비즈니스 로직 처리 Service
 * 로그인, 토큰 검증, 사용자 정보 추출 등의 로직 담당
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;

    @Value("${auth.admin.username}")
    private String adminUsername;

    @Value("${auth.admin.password}")
    private String adminPassword;

    private final PasswordEncoder passwordEncoder;

    private final EmailService emailService;

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

        // 1. DB에서 사용자 조회
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    log.warn("로그인 실패 - 존재하지 않는 사용자: {}", request.getUsername());
                    return new BaseException(ErrorCode.INVALID_CREDENTIALS);
                });

        // 2. 계정 상태 확인
        if (!"ACTIVE".equals(user.getStatus())) {
            log.warn("로그인 실패 - 비활성 계정: username={}, status={}",
                    user.getUsername(), user.getStatus());
            throw new BaseException(ErrorCode.USER_DISABLED, "비활성화된 계정입니다.");
        }

        // 3. 비밀번호 검증 (암호화된 비밀번호와 비교)
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("로그인 실패 - 잘못된 비밀번호: username={}", request.getUsername());
            throw new BaseException(ErrorCode.INVALID_CREDENTIALS);
        }

        // 4. 마지막 로그인 시간 업데이트
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // 5. JWT 토큰 생성
        String token = jwtTokenService.generateToken(user.getUsername());
        log.info("로그인 성공 - username: {}", user.getUsername());

        return LoginResponse.success(token, user.getUsername());
    }

    /**
     * 아이디 찾기
     * 이메일로 사용자 찾아서 아이디 반환
     *
     * @param request 아이디 찾기 요청 (이메일 + 인증코드)
     * @return 찾은 아이디 정보
     */
    public FindUsernameResponse findUsername(FindUsernameRequest request) {
        log.info("아이디 찾기 요청 - email: {}", request.getEmail());

        // 1. 이메일 인증 확인
        boolean verified = emailService.verifyCode(request.getEmail(), request.getVerificationCode());
        if (!verified) {
            throw new BaseException(ErrorCode.INVALID_CREDENTIALS, "이메일 인증에 실패했습니다");
        }

        // 2. 이메일로 사용자 찾기
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BaseException(
                        ErrorCode.USER_NOT_FOUND,
                        "해당 이메일로 가입된 계정이 없습니다"
                ));

        // 3. 응답 생성(일부 가린 아이디)
        String maskedUsername = FindUsernameResponse.maskUsername(user.getUsername());

        log.info("아이디 찾기 성공 - email: {}, username: {}", request.getEmail(), maskedUsername);

        return FindUsernameResponse.builder()
                .username(user.getUsername())
                .maskedUsername(maskedUsername)
                .build();
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
     * @return 유효성(true, false)
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
                    ErrorCode.INVALID_TOKEN
            );
        }
        return jwtTokenService.getUsernameFromToken(token);
    }

    /**
     * 비밀번호 재설정 (임시 비밀번호 발급)
     *
     * @param request 비밀번호 재설정 요청
     * @return 재설정 완료 정보
     */
    public ResetPasswordResponse resetPassword(ResetPasswordRequest request) {
        log.info("비밀번호 재설정 요청 - username: {}, email: {}",
                request.getUsername(), request.getEmail());

        // 1. 이메일 인증 확인
        boolean verified = emailService.verifyCode(request.getEmail(), request.getVerificationCode());
        if (!verified) {
            throw new BaseException(ErrorCode.INVALID_CREDENTIALS, "이메일 인증에 실패했습니다");
        }

        // 2. 아이디 + 이메일로 사용자 찾기
        User user = userRepository.findByUsernameAndEmail(request.getUsername(), request.getEmail())
                .orElseThrow(() -> new BaseException(
                        ErrorCode.USER_NOT_FOUND,
                        "일치하는 회원 정보를 찾을 수 없습니다"
                ));

        // 3. 임시 비밀번호 생성 (8자리, 영문+숫자+특수문자)
        String temporaryPassword = generateTemporaryPassword();
        log.info("임시 비밀번호 생성 완료 - username: {}", request.getUsername());

        // 4. 비밀번호 암호화 후 저장
        user.setPassword(passwordEncoder.encode(temporaryPassword));
        userRepository.save(user);
        log.info("임시 비밀번호 DB 저장 완료 - username: {}", request.getUsername());

        // 5. 이메일로 임시 비밀번호 발송
        emailService.sendTemporaryPassword(request.getEmail(), temporaryPassword);

        log.info("임시 비밀번호 발급 완료 - username: {}", request.getUsername());

        return ResetPasswordResponse.of(request.getEmail());
    }

    /**
     * 임시 비밀번호 생성
     * 회원가입 규칙 준수: 영문(대소문자) + 숫자 + 특수문자 포함, 8자
     * <p>
     * 생성 전략:
     * 1. 영문 대문자 2개
     * 2. 영문 소문자 2개
     * 3. 숫자 2개
     * 4. 특수문자 2개
     * 총 8자 생성 후 섞기
     *
     * @return 임시 비밀번호
     */
    private String generateTemporaryPassword() {
        SecureRandom random = new SecureRandom();

        // 문자 풀 정의
        String upperCaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCaseLetters = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String specialChars = "@$!%*#?&";

        StringBuilder password = new StringBuilder();

        // 1. 각 카테고리에서 최소 2개씩 선택(규칙 보장)
        password.append(getRandomChar(upperCaseLetters, random, 2));
        password.append(getRandomChar(lowerCaseLetters, random, 2));
        password.append(getRandomChar(numbers, random, 2));
        password.append(getRandomChar(specialChars, random, 2));

        // 2. 생성된 문자를 섞기 (순서 예측 방지)
        return shuffleString(password.toString(), random);
    }

    /**
     * 문자열에서 랜덤 문자열 선택
     *
     * @param source 선택할 문자 풀
     * @param random SecureRandom 인스턴스
     * @param count  선택할 개수
     * @return 선택된 문자풀
     */
    private String getRandomChar(String source, SecureRandom random, int count) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < count; i++) {
            result.append(source.charAt(random.nextInt(source.length())));
        }
        return result.toString();
    }

    /**
     * 문자열 섞기 (Fisher-Yates 알고리즘)
     *
     * @param input  원본 문자열
     * @param random SecureRandom 인스턴스
     * @return 섞인 문자열
     */
    private String shuffleString(String input, SecureRandom random) {
        char[] characters = input.toCharArray();

        for (int i = characters.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);

            char temp = characters[i];
            characters[i] = characters[j];
            characters[j] = temp;
        }
        return new String(characters);
    }

    /**
     * 아이디/이메일 일치 여부 확인
     * 보안: 존재 여부만 반환 (상세 정보 노출 안 함)
     *
     * @param email    이메일
     * @param username 아이디
     * @return 일치여부
     */
    public boolean verifyAccountExists(String email, String username) {
        log.info("계정 존재 확인 - username: {}, email: {}", username, email);

        Optional<User> user = userRepository.findByUsernameAndEmail(username, email);
        boolean exists = user.isPresent();

        log.info("계정 존재 여부 - username: {}, exists: {}", username, exists);
        return exists;
    }
}
