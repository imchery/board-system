package study.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import study.auth.config.EmailProperties;
import study.auth.domain.EmailVerification;
import study.auth.repository.EmailVerificationRepository;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 이메일 발송 서비스
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailVerificationRepository verificationRepository;
    private final TemplateEngine templateEngine;
    private final EmailProperties emailProperties;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 인증 코드 생성 및 이메일 발송
     *
     * @param email 수신자 이메일
     */
    public void sendVerificationCode(String email) {

        // 1. 발송 제한 체크 (3분)
        checkSendLimitWithRedis(email);

        // 2. 인증 코드 생성
        String code = generateVerificationCode();

        // 3. 만료 시간 계산(MongoDB 저장)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(emailProperties.getExpirationMinutes());

        // 4. DB 저장
        EmailVerification verification = EmailVerification.builder()
                .email(email)
                .code(code)
                .createdAt(now)
                .expiresAt(expiresAt)
                .verified(false)
                .build();

        verificationRepository.save(verification);
        log.info("인증 코드 생성 완료 - email: {}, code: {}", email, code);

        // 5. Redis에 발송 시간 저장(제한용)
        saveSendTimeToRedis(email);

        // 5. 이메일 발송
        try {
            sendEmail(email, code);
            log.info("인증 이메일 발송 완료 - email: {}", email);
        } catch (Exception e) {
            log.error("인증 이메일 발송 실패 - email: {}", email, e);
            throw new RuntimeException("이메일 발송에 실패했습니다", e);
        }
    }

    /**
     * 발송 제한 체크 (스팸 방지)
     * 3분 내 재발송 불가
     *
     * @param email 이메일
     */
    private void checkSendLimitWithRedis(String email) {
        String key = "send:limit:" + email;
        String lastSendTime = (String) redisTemplate.opsForValue()
                .get(key);

        if (lastSendTime != null) {
            // Redis에 데이터가 있으면 3분이 안 지남
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            throw new RuntimeException(
                    String.format("인증 코드는 3분에 한 번만 발송할 수 있습니다. (%d초 후 재시도)", ttl)
            );
        }
    }

    /**
     * Redis에 발송 시간 저장
     * 3분 후 자동 삭제
     *
     * @param email 이메일
     */
    private void saveSendTimeToRedis(String email) {
        String key = "send:limit:" + email;
        redisTemplate.opsForValue()
                .set(
                        key,
                        LocalDateTime.now()
                                .toString(),
                        3,
                        TimeUnit.MINUTES
                );
    }

    /**
     * 인증 코드 검증
     *
     * @param email 이메일
     * @param code  인증코드
     * @return 검증 성공 여부
     */
    public boolean verifyCode(String email, String code) {

        // 1. 재시도 횟수 체크(Redis)
        checkVerifyAttempts(email);

        // 2. DB에서 조회
        EmailVerification verification = verificationRepository.findByEmailAndCode(email, code)
                .orElseThrow(() -> {
                    incrementVerifyAttempts(email);
                    return new RuntimeException("인증 코드가 일치하지 않습니다");
                });

        // 3. 유효성 검사
        if (!verification.isValid(code)) {
            // 실패 횟수 증가
            incrementVerifyAttempts(email);

            if (verification.isVerified()) {
                throw new RuntimeException("이미 인증된 코드입니다");
            }
            if (verification.isExpired()) {
                throw new RuntimeException("인증 코드가 만료되었습니다");
            }
            throw new RuntimeException("유효하지 않은 인증 코드입니다");
        }

        // 4. 인증 완료 처리
        verification.verify();
        verificationRepository.save(verification);

        // 5. 실패 횟수 초기화
        resetVerifyAttempts(email);

        log.info("이메일 인증 완료 - email: {}", email);
        return true;
    }

    /**
     * 재시도 횟수 체크
     * 5번 실패 시 10분 잠금
     *
     * @param email 이메일
     */
    private void checkVerifyAttempts(String email) {
        String key = "verify:attempts:" + email;
        String attemptsStr = (String) redisTemplate.opsForValue()
                .get(key);

        if (attemptsStr != null) {
            int attempts = Integer.parseInt(attemptsStr);

            // 5번 실패 시 잠금
            if (attempts >= 5) {
                Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
                long minutesLeft = ttl / 60;
                throw new RuntimeException(
                        String.format("인증 실패 5회 초과. %d분 후 다시 시도해주세요.", minutesLeft)
                );
            }
        }
    }

    /**
     * 실패 횟수 증가(Redis)
     *
     * @param email 이메일
     */
    private void incrementVerifyAttempts(String email) {
        String key = "verify:attempts:" + email;

        // 현재 횟수 조회
        String attemptsStr = (String) redisTemplate.opsForValue()
                .get(key);
        int attempts = (attemptsStr != null) ? Integer.parseInt(attemptsStr) : 0;

        // 횟수 증가
        attempts++;
        redisTemplate.opsForValue()
                .set(
                        key,
                        String.valueOf(attempts),
                        10,
                        TimeUnit.MINUTES
                );

        log.warn("인증 실패 - email: {}, 시도 횟수: {}/5", email, attempts);
    }

    /**
     * 실패 횟수 초기화(Redis)
     *
     * @param email 이메일
     */
    private void resetVerifyAttempts(String email) {
        String key = "verify:attempts:" + email;
        redisTemplate.delete(key);

        log.info("인증 성공 - 실패 횟수 초기화: {}", email);
    }

    /**
     * 인증 코드 생성 (6자리 숫자)
     * SecureRandom + Stream 방식
     *
     * @return 인증코드
     */
    private String generateVerificationCode() {
        return new SecureRandom()
                .ints(emailProperties.getCodeLength(), 0, 10)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining());
    }

    /**
     * 이메일 발송
     *
     * @param to   ~에게
     * @param code 코드값
     */
    private void sendEmail(String to, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");

            helper.setFrom(emailProperties.getFromEmail(), emailProperties.getFromName());
            helper.setTo(to);
            helper.setSubject("[" + emailProperties.getFromName() + "] 이메일 인증 코드");

            String htmlContent = buildEmailContent(code);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("이메일 발송 중 오류 발생", e);
            throw new RuntimeException("이메일 발송에 실패했습니다", e);
        }
    }

    /**
     * Thymeleaf로 이메일 HTML 생성
     *
     * @param code 코드값
     * @return 이메일 HTML
     */
    private String buildEmailContent(String code) {
        Context context = new Context();
        context.setVariable("code", code);
        context.setVariable("expirationMinutes", emailProperties.getExpirationMinutes());

        return templateEngine.process("email-verification", context);
    }

    /**
     * 임시 비밀번호 이메일 발송
     *
     * @param to                수신자 이메일
     * @param temporaryPassword 임시 비밀번호
     */
    public void sendTemporaryPassword(String to, String temporaryPassword) {
        log.info("임시 비밀번호 이메일 발송 시작 - email: {}", to);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");

            helper.setFrom(emailProperties.getFromEmail(), emailProperties.getFromName());
            helper.setTo(to);
            helper.setSubject("[" + emailProperties.getFromName() + "] 임시 비밀번호 안내");

            String htmlContent = buildTemporaryPasswordEmailContent(temporaryPassword);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("임시 비밀번호 이메일 발송 완료 - email: {}", to);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("임시 비밀번호 이메일 발송 중 오류 발생 - email: {}", to, e);
            throw new RuntimeException("임시 비밀번호 이메일 발송에 실패했습니다", e);
        }
    }

    /**
     * Thymeleaf로 임시 비밀번호 이메일 HTML 생성
     *
     * @param temporaryPassword 임시 비밀번호
     * @return 이메일 HTML
     */
    private String buildTemporaryPasswordEmailContent(String temporaryPassword) {
        Context context = new Context();
        context.setVariable("temporaryPassword", temporaryPassword);

        return templateEngine.process("temporary-password-email", context);
    }

}
