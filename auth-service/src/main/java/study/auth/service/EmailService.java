package study.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
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

    /**
     * 인증 코드 생성 및 이메일 발송
     *
     * @param email 수신자 이메일
     */
    public void sendVerificationCode(String email) {

        // 1. 발송 제한 체크 (3분)
        checkSendLimit(email);

        // 2. 인증 코드 생성
        String code = generateVerificationCode();

        // 3. 만료 시간 계산
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
    private void checkSendLimit(String email) {
        Optional<EmailVerification> recent = verificationRepository.findTopByEmailOrderByCreatedAtDesc(email);

        if (recent.isPresent()) {
            LocalDateTime lastSendTime = recent.get()
                    .getCreatedAt(); // 최근 날짜 및 시간
            LocalDateTime limitTime = LocalDateTime.now()
                    .minusMinutes(3); // 3분 전

            // 3분이 지나지 않았으면 에러
            if (lastSendTime.isAfter(limitTime)) {

                // 남은 시간 계산
                long secondsLeft = Duration.between(LocalDateTime.now(), lastSendTime.plusMinutes(3))
                        .getSeconds();

                throw new RuntimeException(
                        String.format("인증 코드는 3분에 한 번씩만 발송할 수 있습니다. (%d초 후 재시도)", secondsLeft)
                );
            }
        }

    }

    /**
     * 인증 코드 검증
     *
     * @param email 이메일
     * @param code  인증코드
     * @return 검증 성공 여부
     */
    public boolean verifyCode(String email, String code) {
        EmailVerification verification = verificationRepository.findByEmailAndCode(email, code)
                .orElseThrow(() -> new RuntimeException("인증 코드가 일치하지 않습니다"));

        if (!verification.isValid(code)) {
            if (verification.isVerified()) {
                throw new RuntimeException("이미 인증된 코드입니다");
            }
            if (verification.isExpired()) {
                throw new RuntimeException("인증 코드가 만료되었습니다");
            }
            throw new RuntimeException("유효하지 않은 인증 코드입니다");
        }

        verification.verify();
        verificationRepository.save(verification);
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
        log.debug("인증 시도 체크 - email: {}", email);
    }

    private void incrementVerifyAttempts(String email) {
        log.warn("인증 실패 - email: {}", email);
    }

    private void resetVerifyAttempts(String email) {
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
        context.setVariable("verificationCode", emailProperties.getExpirationMinutes());

        return templateEngine.process("email-verification", context);
    }
}
