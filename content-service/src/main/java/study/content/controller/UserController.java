package study.content.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import study.common.lib.exception.BaseException;
import study.common.lib.exception.ErrorCode;
import study.common.lib.response.PageResponse;
import study.common.lib.response.ResponseVO;
import study.content.dto.comment.CommentResponse;
import study.content.dto.post.PostResponse;
import study.content.dto.user.UserStatsResponse;
import study.content.service.UserService;

/**
 * 사용자 관련 API Controller
 * 마이페이지 기능 제공(통계, 내가 쓴 글/댓글 조회)
 * TODO: 향후 user-service로 이관 예정
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 내 통계 정보 조회 (마이페이지 대시보드용)
     * 작성한 게시글 수, 댓글 수를 반환
     *
     * @param httpRequest HTTP 요청 (username 포함)
     * @return 사용자 통계 정보
     */
    @GetMapping("/my-stats")
    public ResponseVO<UserStatsResponse> getMyState(HttpServletRequest httpRequest) {

        // 인증 확인 (마이페이지는 로그인 필수)
        String username = extractUsername(httpRequest);
        log.info("마이페이지 통계 조회: username: {}", username);

        UserStatsResponse stats = userService.getUserStats(username);
        return ResponseVO.ok(stats);
    }

    /**
     * 내가 작성한 게시글 목록 조회(마이페이지용)
     *
     * @param page        페이지 번호
     * @param size        페이지 크기
     * @param httpRequest HTTP 요청(username 포함)
     * @return 내가 쓴 게시글 목록
     */
    @GetMapping("/my-posts")
    public ResponseVO<PageResponse<PostResponse>> getMyPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest
    ) {
        // 인증 확인
        String username = extractUsername(httpRequest);

        log.info("내가 쓴 글 조회: username: {}, page: {}, size: {}", username, page, size);

        PageResponse<PostResponse> myPosts = userService.getMyPosts(username, page, size);
        return ResponseVO.ok(myPosts);
    }

    /**
     * 내가 작성한 댓글 목록 조회(마이페이지용)
     *
     * @param page        페이지 번호
     * @param size        페이지 크기
     * @param httpRequest HTTP 요청(username 포함)
     * @return 내가 쓴 댓글 목록
     */
    @GetMapping("/my-comments")
    public ResponseVO<PageResponse<CommentResponse>> getMyComments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest
    ) {
        String username = extractUsername(httpRequest);

        log.info("내가 쓴 댓글 조회: username: {}, page: {}, size: {}", username, page, size);

        PageResponse<CommentResponse> myComments = userService.getMyComments(username, page, size);
        return ResponseVO.ok(myComments);
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
            log.warn("인증되지 않은 마이페이지 요청");
            throw new BaseException(ErrorCode.UNAUTHORIZED);
        }

        return username;
    }
}
