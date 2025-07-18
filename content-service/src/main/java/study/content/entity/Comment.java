package study.content.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "comments")
public class Comment {

    @Id
    private String id;

    /**
     * 댓글이 달린 게시글 ID (Post와의 관계)
     */
    private String postId;

    /**
     * 댓글 내용
     */
    private String content;

    /**
     * 댓글 작성자 (JWT 토큰에서 추출)
     */
    private String author;

    /**
     * 좋아요 수
     */
    private Integer likeCount;

    /**
     * 생성일시
     */
    @CreatedDate
    private LocalDateTime createdAt;

    /**
     * 수정일시
     */
    @LastModifiedDate
    private LocalDateTime updatedAt;

    /**
     * 댓글 상태 (ACTIVE, DELETED)
     */
    private CommentStatus status;

    /**
     * 대댓글을 위한 부모 댓글 ID (향후 확장용)
     */
    private String parentCommentId;

    public enum CommentStatus {
        ACTIVE, DELETED;
    }

    // 빌더 패턴용 기본값 설정
    public static CommentBuilder builder() {
        return new CommentBuilder()
                .likeCount(0)
                .status(CommentStatus.ACTIVE);
    }

    // =========================================== 비즈니스 메서드 ==============================================

    /**
     * 댓글 수정
     *
     * @param content
     */
    public void updateContent(String content) {
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 좋아요 증가
     */
    public void increaseLikeCount() {
        this.likeCount++;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 좋아요 감소
     */
    public void decreaseLikeCount() {
        this.likeCount--;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 댓글 삭제 (Soft Delete)
     */
    public void delete() {
        this.status = CommentStatus.DELETED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 작성자 권한 확인
     *
     * @param username
     * @return
     */
    public boolean isAuthor(String username) {
        return this.author != null && this.author.equals(username);
    }

    /**
     * 활성화 상태 확인
     *
     * @return
     */
    public boolean isActive() {
        return this.status == CommentStatus.ACTIVE;
    }

    /**
     * 삭제 상태 확인
     *
     * @return
     */
    public boolean isDeleted() {
        return this.status == CommentStatus.DELETED;
    }

    /**
     * 대댓글인지 확인
     *
     * @return
     */
    public boolean isReply() {
        return this.parentCommentId != null;
    }

    /**
     * 최상위 댓글인지 확인
     *
     * @return
     */
    public boolean isRootComment() {
        return this.parentCommentId == null;
    }


}
