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

@Document(collection = "post_likes")
@CompoundIndexes({
        @CompoundIndex(
                name = "post_user_unique_idx",
                def = "{'postId': 1, 'username': 1}", // 한 사용자가 같은 댓글에 중복 좋아요 방지
                unique = true // 중복 좋아요 방지
        ),
        @CompoundIndex(
                name = "post_create_idx",
                def = "{'postId': 1, 'createdAt': -1}" // 특정 댓글의 좋아요 목록을 최신순으로 조회
        )
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostLike {

    @Id
    private String id;

    /**
     * 게시글 ID
     */
    private String postId;

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
     * 게시글 좋아요 생성
     *
     * @param postId   게시글 ID
     * @param username 사용자명
     * @return PostLike 엔티티
     */
    public static PostLike create(String postId, String username) {
        PostLike postLike = new PostLike();
        postLike.postId = postId;
        postLike.username = username;
        postLike.createdAt = LocalDateTime.now();
        return postLike;
    }

    @Override
    public String toString() {
        return "PostLike{" +
                "id='" + id + '\'' +
                ", postId='" + postId + '\'' +
                ", username='" + username + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
