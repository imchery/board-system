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
import study.content.service.PostLikeService;

@Slf4j
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostLikeController {

    private final PostLikeService postLikeService;

    @PostMapping("/{postId}/like")
    public ResponseVO togglePostLike(
            @PathVariable String postId,
            HttpServletRequest httpRequest
    ) {
        // 1. 인증 확인 (로그인 필수)
        String username = (String) httpRequest.getAttribute("username");
        if (username == null) {
            log.warn("게시글 좋아요 요청 - 인증 실패: postId: {}", postId);
            return ResponseVO.authFail();
        }
        log.info("게시글 좋아요 토글 요청: postId: {}, username: {}", postId, username);

        try {
            // 2. 좋아요 토글 실행
            boolean isLiked = postLikeService.togglePostLike(postId, username);

            // 3. 토글 후 좋아요 개수 조회
            long likeCount = postLikeService.getPostLikeCount(postId);

            // 4. 댓글 정보 조회
            LikeResponse response = LikeResponse.builder()
                    .id(postId)
                    .likeCount(likeCount)
                    .isLikedByCurrentUser(isLiked)
                    .build();

            log.info("게시글 좋아요 토글 완료: postId: {}, username: {}, isLiked: {}, likeCount: {}",
                    postId, username, isLiked, likeCount);

            return ResponseVO.ok(response);

        } catch (Exception e) {
            log.error("게시글 좋아요 토글 실패: postId: {}, username: {}, error: {}",
                    postId, username, e.getMessage(), e);
            return ResponseVO.error("게시글 좋아요 처리에 실패했습니다.");
        }
    }
}
