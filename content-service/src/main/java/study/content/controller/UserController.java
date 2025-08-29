package study.content.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import study.common.lib.response.PageResponse;
import study.common.lib.response.ResponseVO;
import study.content.dto.comment.CommentResponse;
import study.content.dto.post.PostResponse;
import study.content.dto.user.UserStatsResponse;
import study.content.service.UserService;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 내 통계 정보 조회 (마이페이지 대시보드용)
     * TODO user-service로 이관 예정
     *
     * @param httpRequest
     * @return
     */
    @GetMapping("/my-stats")
    public ResponseVO getMyState(HttpServletRequest httpRequest) {

        // 1. 인증 확인 (마이페이지는 로그인 필수)
        String username = (String) httpRequest.getAttribute("username");
        if (username == null) {
            return ResponseVO.authFail();
        }
        log.info("마이페이지 통계 조회: username: {}", username);

        try {
            UserStatsResponse stats = userService.getUserStats(username);
            return ResponseVO.ok(stats);
        } catch (Exception e) {
            log.error("마이페이지 통계 조회 실패: username: {}, error: {}",
                    username, e.getMessage(), e);
            return ResponseVO.error("통계 정보 조회에 실패했습니다.");
        }
    }

    /**
     * 내가 작성한 게시글 목록 조회(마이페이지용)
     *
     * @param page
     * @param size
     * @param httpRequest
     * @return
     */
    @GetMapping("/my-posts")
    public ResponseVO getMyPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest
    ) {
        // 1. 인증 확인
        String username = (String) httpRequest.getAttribute("username");
        if (username == null) {
            return ResponseVO.authFail();
        }

        log.info("내가 쓴 글 조회: username: {}, page: {}, size: {}", username, page, size);

        try {
            PageResponse<PostResponse> myPosts = userService.getMyPosts(username, page, size);
            return ResponseVO.ok(myPosts);
        } catch (Exception e) {
            log.error("내가 쓴 글 조회 실패: username: {}, error: {}",
                    username, e.getMessage(), e);
            return ResponseVO.error("게시글 목록 조회에 실패했습니다.");
        }
    }

    /**
     * 내가 작성한 댓글 목록 조회(마이페이지용)
     *
     * @param page
     * @param size
     * @param httpRequest
     * @return
     */
    @GetMapping("/my-comments")
    public ResponseVO getMyComments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest
    ) {
        String username = (String) httpRequest.getAttribute("username");
        if (username == null) {
            return ResponseVO.authFail();
        }
        log.info("내가 쓴 댓글 조회: username: {}, page: {}, size: {}", username, page, size);
        try {
            PageResponse<CommentResponse> myComments = userService.getMyComments(username, page, size);
            return ResponseVO.ok(myComments);
        } catch (Exception e) {
            log.error("내가 쓴 댓글 조회 실패: username: {}, error: {}",
                    username, e.getMessage(), e);
            return ResponseVO.error("댓글 목록 조회에 실패했습니다.");
        }
    }
}
