package study.content.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatsResponse {

    /**
     * 작성한 게시글 수
     */
    private Long postCount;

    /**
     * 작성한 댓글 수 (대댓글 포함)
     */
    private Long commentCount;

    /**
     * 사용자명
     */
    private String username;
}
