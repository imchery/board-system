package study.content.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import study.content.entity.Comment;

/**
 * 댓글 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponse {

    // ======================= 기존 댓글 정보 =======================
    private String id;
    private String postId;
    private String content;
    private String author;
    private String createdAt;
    private String updatedAt;
    private String parentCommentId;

    // ======================= 좋아요 관련 정보 =======================
    /**
     * 해당 댓글의 총 좋아요 개수
     */
    private Long likeCount;

    /**
     * 현재 로그인한 사용자가 이 댓글에 좋아요를 눌렀는지 여부
     */
    private Boolean isLikedByCurrentUser;

    /**
     * 댓글 타입(최상위 댓글인지 대댓글인지)
     * 참조형이므로 null
     */
    private Boolean isReply;

    // ======================= 변환 메서드 =======================

    /**
     * 기본 댓글 정보만 포함한 DTO 변환
     * 좋아요 정보는 NULL로 설정
     *
     * @param comment 댓글 엔티티
     * @return 기본정보만 포함된 CommentResponse
     */
    public static CommentResponse from(Comment comment) {
        return CommentResponse.builder()
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
                .likeCount(null)
                .isLikedByCurrentUser(null)
                .build();
    }

    /**
     * 좋아요 정보를 포함한 DTO 변환
     *
     * @param comment              댓글 엔티티
     * @param likeCount            좋아요 개수
     * @param isLikedByCurrentUser 현재 사용자의 좋아요 여부
     * @return 좋아요 정보가 포함된 CommentResponse
     */
    public static CommentResponse withLikes(Comment comment, long likeCount, boolean isLikedByCurrentUser) {
        return CommentResponse.builder()
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
                .likeCount(likeCount)
                .isLikedByCurrentUser(isLikedByCurrentUser)
                .build();
    }

    /**
     * 로그인하지 않은 사용자용 (좋아요 개수만 포함)
     *
     * @param comment   댓글 엔티티
     * @param likeCount 좋아요 개수
     * @return 좋아요 정보가 포함된 CommentResponse
     */
    public static CommentResponse withLikeCount(Comment comment, long likeCount) {
        return withLikes(comment, likeCount, false);
    }
}
