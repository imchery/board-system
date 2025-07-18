package study.content.dto.comment;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentStatsResponse {

    /**
     * 전체 댓글 수
     */
    private long totalComments;

    /**
     * 최상위 댓글 수
     */
    private long rootComments;

    /**
     * 대댓글 수
     */
    private long replies;
}
