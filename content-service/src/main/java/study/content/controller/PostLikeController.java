package study.content.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import study.common.lib.exception.BaseException;
import study.common.lib.exception.ErrorCode;
import study.common.lib.response.ResponseVO;
import study.content.dto.comment.LikeResponse;
import study.content.entity.Like;
import study.content.service.LikeService;

import java.util.List;
import java.util.Map;

/**
 * 게시글 좋아요 관련 API Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostLikeController {

    private final LikeService likeService;

    /**
     * 게시글 좋아요 토글
     * 좋아요가 없으면 추가, 있으면 제거
     *
     * @param postId      게시글 ID
     * @param httpRequest 사용자명 추출
     * @return 좋아요 정보
     */
    @PostMapping("/{postId}/like")
    public ResponseVO<LikeResponse> togglePostLike(
            @PathVariable String postId,
            HttpServletRequest httpRequest
    ) {
        // 1. 인증 확인 (로그인 필수)
        String username = extractUsername(httpRequest);

        log.info("게시글 좋아요 토글 요청: postId: {}, username: {}", postId, username);

        LikeResponse response = likeService.toggleLikeAndGetInfo(postId, Like.TargetType.POST, username);
        return ResponseVO.ok(response);
    }

    /**
     * 여러 개의 좋아요 개수 일괄 조회
     *
     * @param postIds 게시글 ID 목록
     * @return 게시글 ID별 좋아요 개수
     */
    @PostMapping("/bulk-like-counts")
    public ResponseVO<Map<String, Long>> getBulkPostLikeCounts(
            @RequestBody List<String> postIds
    ) {
        validatePostIds(postIds);

        log.debug("게시글 좋아요 일괄 조회 - count: {}", postIds.size());

        Map<String, Long> likeCounts = likeService.getBulkLikeCount(postIds, Like.TargetType.POST);
        return ResponseVO.ok(likeCounts);
    }

    /**
     * 특정 게시글의 좋아요 정보 조회
     *
     * @param postId      게시글 ID
     * @param httpRequest 사용자명
     * @return 게시글별 좋아요 개수
     */
    @GetMapping("/{postId}/like-info")
    public ResponseVO<LikeResponse> getPostLikeInfo(
            @PathVariable String postId,
            HttpServletRequest httpRequest
    ) {
        String username = (String) httpRequest.getAttribute("username");

        log.debug("게시글 좋아요 정보 조회 - postId: {}, user: {}",
                postId, username != null ? username : "비로그인");

        LikeResponse response = likeService.getLikeInfo(postId, Like.TargetType.POST, username);
        return ResponseVO.ok(response);
    }

    /**
     * HTTP 요청에서 username 추출 및 검증
     *
     * @param request HTTP 요청
     * @return username
     */
    private String extractUsername(HttpServletRequest request) {
        String username = (String) request.getAttribute("username");

        if (username == null) {
            log.warn("인증되지 않은 좋아요 유형");
            throw new BaseException(ErrorCode.UNAUTHORIZED);
        }

        return username;
    }

    /**
     * 게시글 ID 목록 검증
     *
     * @param postIds 게시글 ID 목록
     */
    private void validatePostIds(List<String> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            log.warn("빈 게시글 ID 목록");
            throw new BaseException(ErrorCode.INVALID_REQUEST, "게시글 ID 목록이 필요합니다.");
        }
    }
}
