package study.content.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequest {

    @NotNull(message = "게시글 ID는 필수입니다")
    @NotBlank(message = "게시글 ID는 필수입니다")
    private String postId;

    @NotBlank(message = "댓글 내용은 필수입니다")
    @Size(max = 1000, message = "댓글 내용은 1000자 이하로 입력해주세요")
    private String content;

    /**
     * 대댓글을 위한 부모 댓글 ID
     * null이면 최상위 댓글, 값이 있으면 대댓글
     */
    private String parentCommentId;
}
