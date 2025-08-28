package study.content.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import study.common.lib.response.ResponseVO;
import study.content.dto.comment.LikeResponse;
import study.content.service.PostLikeService;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostLikeController {

    private final PostLikeService postLikeService;

    /**
     * 게시글 목록 및 상세화면 좋아요 정보
     *
     * @param postId      게시글 ID
     * @param httpRequest 사용자명 추출
     * @return 좋아요 정보
     */
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
            LikeResponse response = postLikeService.togglePostLikeAndGetInfo(postId, username);
            return ResponseVO.ok(response);

        } catch (Exception e) {
            log.error("게시글 좋아요 토글 실패: postId: {}, username: {}, error: {}",
                    postId, username, e.getMessage(), e);
            return ResponseVO.error("게시글 좋아요 처리에 실패했습니다.");
        }
    }

    /**
     * 여러 개의 좋아요 개수 일괄 조회
     *
     * @param postIds 게시글 ID 목록
     * @return 게시글 ID별 좋아요 개수
     */
    @PostMapping("/bulk-like-counts")
    public ResponseVO getBulkPostLikeCounts(
            @RequestBody List<String> postIds
    ) {
        // 1. 빈 배열 체크
        if (postIds == null || postIds.isEmpty()) {
            return ResponseVO.error("게시글 ID 목록이 필요합니다.");
        }

        log.info("게시글 좋아요 개수 일괄 조회: postIds count: {}", postIds.size());

        try {
            Map<String, Long> likeCounts = postLikeService.getBulkLikeCounts(postIds);
            return ResponseVO.ok(likeCounts);
        } catch (Exception e) {
            log.error("게시글 좋아요 일괄 조회 실패: error: {}", e.getMessage(), e);
            return ResponseVO.error("좋아요 정보 조회에 실패했습니다.");
        }
    }

    /**
     * 특정 게시글의 좋아요 정보 조회
     *
     * @param postId 게시글 ID
     * @param httpRequest 사용자명
     * @return 게시글별 좋아요 개수
     */
    @GetMapping("/{postId}/like-info")
    public ResponseVO getPostLikeInfo(
            @PathVariable String postId,
            HttpServletRequest httpRequest
    ) {
        String username = (String) httpRequest.getAttribute("username");

        try {
            LikeResponse response = postLikeService.getPostLikeInfo(postId, username);
            return ResponseVO.ok(response);
        } catch (Exception e) {
            log.error("좋아요 정보 조회 실패: {}", e.getMessage());
            return ResponseVO.error("좋아요 정보 조회에 실패했습니다.");
        }
    }
}
