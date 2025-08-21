package study.content.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import study.content.entity.Comment;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentWithLikeResponse {

    // ======================= 기존 댓글 정보 =======================
    private String id;
    private String postId;
    private String content;
    private String author;
    private String createdAt;
    private String updatedAt;
    private String parentCommentId;
    private Boolean isReply;

    // ======================= 좋아요 관련 정보 =======================

    /**
     * 해당 댓글의 총 좋아요 개수
     */
    private long likeCount;

    /**
     * 현재 로그인한 사용자가 이 댓글에 좋아요를 눌렀는지 여부
     */
    private boolean isLikedByCurrentUser;

    // ======================= 변환 메서드 =======================

    /**
     * Comment 엔티티 + 좋아요 정보 → DTO 변환
     * 로그인된 사용자 전용 (좋아요 기능은 로그인 필수)
     *
     * @param comment              댓글 엔티티
     * @param likeCount            좋아요 개수
     * @param isLikedByCurrentUser 현재 사용자의 좋아요 여부
     * @return CommentWithLikeResponse
     */
    public static CommentWithLikeResponse from(Comment comment, long likeCount, boolean isLikedByCurrentUser) {
        return CommentWithLikeResponse.builder()
                // 기존 댓글 정보
                .id(comment.getId())
                .postId(comment.getPostId())
                .content(comment.getContent())
                .author(comment.getAuthor())
                .createdAt(comment.getCreatedAt()
                        .toString())
                .updatedAt(comment.getUpdatedAt()
                        .toString())
                .parentCommentId(comment.getParentCommentId())
                .isReply(comment.getParentCommentId() != null)
                // 좋아요 정보 (로그인 사용자만)
                .likeCount(likeCount)
                .isLikedByCurrentUser(isLikedByCurrentUser)
                .build();
    }
}
