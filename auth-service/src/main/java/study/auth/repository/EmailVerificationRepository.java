package study.auth.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import study.auth.domain.EmailVerification;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 이메일 인증 Repository
 */
@Repository
public interface EmailVerificationRepository extends MongoRepository<EmailVerification, String> {

    /**
     * 이메일로 가장 최근 인증 정보 조회
     *
     * @param email 이메일
     * @return 인증정보
     */
    Optional<EmailVerification> findTopByEmailOrderByCreatedAtDesc(String email);

    /**
     * 이메일과 인증 코드로 조회
     *
     * @param email 이메일
     * @param code  인증코드
     * @return 인증 정보
     */
    Optional<EmailVerification> findByEmailAndCode(String email, String code);

    /**
     * 만료된 인증 정보 삭제
     *
     * @param expiresAt 만료 시간
     */
    void deleteByExpiresAtBefore(LocalDateTime expiresAt);

    /**
     * 이메일로 모든 인증 정보 삭제
     *
     * @param email 이메일
     */
    void deleteByEmail(String email);
}
