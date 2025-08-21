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
    private String commentId;

    /**
     * 현재 좋아요 상태 (true: 좋아요, false: 취소)
     */
    private boolean isLiked;

    /**
     * 해당 댓글의 총 좋아요 개수
     */
    private long likeCount;

    /**
     * 메시지 (UI 에서 토스트로 보여줄 용도)
     */
    private String message;

    // ======================= 정적 팩토리 메서드 =======================

    /**
     * 좋아요 생성된 경우의 응답
     *
     * @param commentId 댓글 ID
     * @param likeCount 좋아요 개수
     * @return
     */
    public static CommentLikeResponse liked(String commentId, long likeCount) {
        return CommentLikeResponse.builder()
                .commentId(commentId)
                .isLiked(true)
                .likeCount(likeCount)
                .message("댓글에 좋아요를 눌렀습니다.")
                .build();
    }

    /**
     * 좋아요 취소된 경우의 응답
     * @param commentId 댓글 ID
     * @param likeCount 좋아요 개수
     * @return
     */
    public static CommentLikeResponse unLiked(String commentId, long likeCount) {
        return CommentLikeResponse.builder()
                .commentId(commentId)
                .isLiked(false)
                .likeCount(likeCount)
                .message("댓글 좋아요를 취소했습니다.")
                .build();
    }

}
