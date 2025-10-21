package study.content.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import study.common.lib.exception.BaseException;
import study.common.lib.exception.ErrorCode;
import study.common.lib.response.ResponseVO;
import study.content.dto.comment.LikeResponse;
import study.content.entity.Like;
import study.content.service.LikeService;

/**
 * 댓글 좋아요 관련 API Controller
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentLikeController {

    private final LikeService likeService;

    /**
     * 댓글 좋아요 토글
     * 좋아요가 없으면 추가, 있으면 제거
     *
     * @param commentId   댓글 ID
     * @param httpRequest HTTP 요청 (JWT 에서 username 추출)
     * @return CommentLikeResponse (좋아요 상태 + 개수)
     */
    @PostMapping("/comments/{commentId}/like")
    public ResponseVO<LikeResponse> toggleCommentLike(
            @PathVariable String commentId,
            HttpServletRequest httpRequest
    ) {
        // 1. 인증 확인 (로그인 필수)
        String username = (String) httpRequest.getAttribute("username");
        if (username == null) {
            log.warn("인증되지 않은 좋아요 요청");
            throw new BaseException(ErrorCode.UNAUTHORIZED);
        }
        log.info("댓글 좋아요 토글 요청: commentId: {}, username: {}", commentId, username);

        LikeResponse response = likeService.toggleLikeAndGetInfo(commentId, Like.TargetType.COMMENT, username);
        return ResponseVO.ok(response);
    }
}
