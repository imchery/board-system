package study.content.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import study.common.lib.response.ResponseVO;
import study.content.dto.comment.LikeResponse;
import study.content.entity.Like;
import study.content.service.LikeService;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentLikeController {

    private final LikeService likeService;

    /**
     * 댓글 좋아요 토글 (생성/삭제)
     *
     * @param commentId   댓글 ID
     * @param httpRequest HTTP 요청 (JWT 에서 username 추출)
     * @return CommentLikeResponse (좋아요 상태 + 개수)
     */
    @PostMapping("/comments/{commentId}/like")
    public ResponseVO toggleCommentLike(
            @PathVariable String commentId,
            HttpServletRequest httpRequest
    ) {
        // 1. 인증 확인 (로그인 필수)
        String username = (String) httpRequest.getAttribute("username");
        if (username == null) {
            log.warn("댓글 좋아요 요청 - 인증 실패: commentId: {}", commentId);
            return ResponseVO.authFail();
        }
        log.info("댓글 좋아요 토글 요청: commentId: {}, username: {}", commentId, username);

        try {
            LikeResponse response = likeService.toggleLikeAndGetInfo(commentId, Like.TargetType.COMMENT, username);
            return ResponseVO.ok(response);
        } catch (Exception e) {
            log.error("댓글 좋아요 토글 실패: commentId: {}, username: {}, error: {}",
                    commentId, username, e.getMessage(), e);
            return ResponseVO.error("댓글 좋아요 처리에 실패했습니다.");
        }
    }
}
