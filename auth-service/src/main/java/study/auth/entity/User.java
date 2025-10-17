package study.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * 회원 엔티티
 */
@Document(collection = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    private String id;

    @Indexed(unique = true) // 유니크 인덱스 자동 생성
    private String username;

    private String password; // BCrypt 암호화된 비밀번호

    @Indexed(unique = true)
    private String email;

    @Indexed // 일반 인덱스 (검색 최적화)
    private String nickname;

    private String profileImage; // 프로필 이미지 URL (null 허용)

    @Builder.Default
    private String role = "USER"; // USER | ADMIN

    @Builder.Default
    private String status = "ACTIVE"; // ACTIVE | INACTIVE | SUSPENDED

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime lastLoginAt; // 마지막 로그인 시간
}
