package study.content.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentLikeResponse {

    /**
     * 댓글 ID
     */
    private String id;

    /**
     * 좋아요 개수
     */
    private Long likeCount;

    /**
     * 사용자의 좋아요 상태 (좋아요: true / 취소: false)
     */
    private Boolean isLikedByCurrentUser;
}
