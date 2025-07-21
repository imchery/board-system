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
public class CommentResponse {

    private String id;
    private String postId;
    private String content;
    private String author;
    private String createdAt;
    private String updatedAt;
    private String parentCommentId;

    /**
     * 댓글 타입(최상위 댓글인지 대댓글인지)
     * 참조형이므로 null
     */
    private Boolean isReply;

    // Entity -> DTO 변환
    public static CommentResponse form(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .postId(comment.getPostId())
                .content(comment.getContent())
                .author(comment.getAuthor())
                .createdAt(comment.getCreatedAt().toString())
                .updatedAt(comment.getUpdatedAt().toString())
                .parentCommentId(comment.getParentCommentId())
                .isReply(comment.getParentCommentId() != null ? true : null)
                .build();
    }
}
