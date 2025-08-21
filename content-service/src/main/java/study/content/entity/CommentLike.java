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

@Document(collection = "comment_likes")
@CompoundIndexes({
        @CompoundIndex(
                name = "comment_user_unique_idx",
                def = "{'commentId': 1, 'username': 1}",
                unique = true // 중복 좋아요 방지
        ),
        @CompoundIndex(
                name = "comment_create_idx",
                def = "{'commentId: 1, 'createAt': -1}" // 댓글별 좋아요 목록 조회 최적화
        )
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentLike {

    @Id
    private String id;

    /**
     * 댓글 ID (Comment와 연관)
     */
    private String commentId;

    /**
     * 좋아요를 누른 사용자명
     */
    private String username;

    /**
     * 좋아요 생성 시간
     */
    @CreatedDate
    private LocalDateTime createdAt;

    // ======================= 생성 메서드 =======================

    /**
     * 댓글 좋아요 생성
     *
     * @param commentId 댓글 ID
     * @param username  사용자명
     * @return CommentLike 엔티티
     */
    public static CommentLike create(String commentId, String username) {
        CommentLike commentLike = new CommentLike();
        commentLike.commentId = commentId;
        commentLike.username = username;
        commentLike.createdAt = LocalDateTime.now();
        return commentLike;
    }

    // ======================= 비즈니스 로직 =======================

    /**
     * 특정 사용자의 좋아요인지 확인
     *
     * @param username 확인할 사용자명
     * @return 해당 사용자의 좋아요 여부
     */
    public boolean isLikedBy(String username) {
        return this.username != null && this.username.equals(username);
    }

    /**
     * 특정 댓글의 좋아요인지 확인
     *
     * @param commentId 확인할 댓글 ID
     * @return 해당 댓글의 좋아요 여부
     */
    public boolean isForComment(String commentId) {
        return this.commentId != null && this.commentId.equals(commentId);
    }

    // ======================= 디버깅용 toString =======================

    @Override
    public String toString() {
        return "CommentLike{" +
                "id='" + id + '\'' +
                ", commentId='" + commentId + '\'' +
                ", username='" + username + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }

}
