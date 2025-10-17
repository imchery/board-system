package study.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.auth.dto.SignupRequest;
import study.auth.dto.SignupResponse;
import study.auth.entity.User;
import study.auth.repository.UserRepository;
import study.common.lib.exception.BaseException;
import study.common.lib.exception.ErrorCode;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입
     *
     * @param request 회원가입 요청 DTO
     * @return 회원가입 응답 DTO
     */
    @Transactional
    public SignupResponse signup(SignupRequest request) {
        log.info("회원가입 시도: username={}", request.getUsername());

        // 1. 아이디 중복체크
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("중복된 아이디: {}", request.getUsername());
            throw new BaseException(ErrorCode.DUPLICATE_USERNAME);
        }

        // 2. 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("중복된 이메일: {}", request.getEmail());
            throw new BaseException(ErrorCode.DUPLICATE_EMAIL);
        }

        // 3. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        log.debug("비밀번호 암호화 완료");

        // 4. User 엔티티 생성
        User user = User.builder()
                .username(request.getUsername())
                .password(encodedPassword)
                .email(request.getEmail())
                .nickname(request.getNickname())
                .role("USER")
                .status("ACTIVE")
                .build();

        // 5. DB 저장
        User savedUser = userRepository.save(user);
        log.info("회원가입 완료: id={}, username={}", savedUser.getId(), savedUser.getUsername());

        // 6. 응답 생성
        return SignupResponse.builder()
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .nickname(savedUser.getNickname())
                .message("회원가입이 완료되었습니다")
                .build();
    }

    /**
     * 아이디 중복 체크
     *
     * @param username 확인할 아이디
     * @return 중복여부 (true: 사용가능, false: 중복)
     */
    public boolean checkUsernameDuplicate(String username) {
        boolean exists = userRepository.existsByUsername(username);
        log.debug("아이디 중복 체크: username={}, exists={}", username, exists);
        return !exists;
    }

    /**
     * 이메일 중복 체크
     *
     * @param email 확인할 이메일
     * @return 중복여부 (true: 사용가능, false: 중복)
     */
    public boolean checkEmailDuplicate(String email) {
        boolean exists = userRepository.existsByEmail(email);
        log.debug("이메일 중복 체크: email={}, exists={}", email, exists);
        return !exists;
    }
}
