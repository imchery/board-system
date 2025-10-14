package study.content.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "likes")
@CompoundIndexes({
        @CompoundIndex(
                name = "target_user_unique_idx",
                def = "{'targetId': 1, 'targetType': 1, 'username': 1}", // 3개 조합으로 유니크
                unique = true
        ),
        @CompoundIndex(
                name = "target_type_created_idx",
                def = "{'targetId': 1, 'targetType': 1, 'createdAt': -1}" // 특정 대상의 좋아요 목록 조회용
        ),
        @CompoundIndex(
                name = "username_created_idx",
                def = "{'username': 1, 'createdAt': -1}" // 사용자별 좋아요 목록 조회용
        )
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Like {

    @Id
    private String id;

    /**
     * 좋아요 대상 ID (게시글 ID 또는 댓글 ID)
     */
    private String targetId;

    /**
     * 좋아요 대상 타입 (POST, COMMENT)
     */
    private TargetType targetType;

    /**
     * 좋아요를 누른 사용자명
     */
    private String username;

    /**
     * 좋아요 생성 시간
     */
    @CreatedDate
    private LocalDateTime createdAt;

    public enum TargetType {
        POST,
        COMMENT
    }

    /**
     * 좋아요 생성 (정적 팩토리 메서드)
     *
     * @param targetId   대상 ID
     * @param targetType 대상 타입
     * @param username   사용자명
     * @return Like 엔티티
     */
    public static Like create(String targetId, TargetType targetType, String username) {
        Like like = new Like();
        like.targetId = targetId;
        like.targetType = targetType;
        like.username = username;
        like.createdAt = LocalDateTime.now();
        return like;
    }

    /**
     * 게시글 좋아요인지 확인
     *
     * @return
     */
    public boolean isPostLike() {
        return this.targetType == TargetType.POST;
    }

    /**
     * 댓글 좋아요인지 확인
     *
     * @return
     */
    public boolean isCommentLike() {
        return this.targetType == TargetType.COMMENT;
    }

    @Override
    public String toString() {
        return "Like{" +
                "id='" + id + '\'' +
                ", targetId='" + targetId + '\'' +
                ", targetType=" + targetType +
                ", username='" + username + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
