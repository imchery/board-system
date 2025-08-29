package study.content.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import study.common.lib.response.PageResponse;
import study.common.lib.response.ResponseVO;
import study.content.dto.comment.CommentRequest;
import study.content.dto.comment.CommentResponse;
import study.content.dto.comment.CommentUpdateRequest;
import study.content.service.CommentService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // -----------------------------------------------------------------------------------------------------------------
    //                                                  생성/수정/삭제
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * 댓글 생성
     *
     * @param postId
     * @param request
     * @param httpRequest
     * @return
     */
    @PostMapping("/posts/{postId}/comments")
    public ResponseVO createComment(@PathVariable String postId,
                                    @Valid @RequestBody CommentRequest request,
                                    HttpServletRequest httpRequest) {
        String username = (String) httpRequest.getAttribute("username");
        if (username == null) {
            return ResponseVO.authFail();
        }

        // postId 일치 확인
        if (!postId.equals(request.getPostId())) {
            return ResponseVO.error("URL의 게시글 ID와 요청 데이터가 일치하지 않습니다.");
        }

        log.info("댓글 생성 요청: postId: {}, username: {}", postId, username);
        CommentResponse comment = commentService.createComment(request, username);
        return ResponseVO.saveOk(comment);
    }

    /**
     * 댓글 수정
     *
     * @param commentId
     * @param request
     * @param httpRequest
     * @return
     */
    @PutMapping("/comments/{commentId}")
    public ResponseVO updateComment(@PathVariable String commentId,
                                    @Valid @RequestBody CommentUpdateRequest request,
                                    HttpServletRequest httpRequest) {
        String username = (String) httpRequest.getAttribute("username");
        if (username == null) {
            return ResponseVO.authFail();
        }

        log.info("댓글 수정 요청: commentId: {}, username: {}", commentId, username);
        try {
            CommentResponse comment = commentService.updateComment(commentId, request, username);
            return ResponseVO.updateOk(comment);
        } catch (IllegalArgumentException e) {
            return ResponseVO.accessDenied();
        } catch (RuntimeException e) {
            return ResponseVO.error("댓글 수정에 실패했습니다.");
        }
    }

    /**
     * 댓글 삭제
     *
     * @param commentId
     * @param httpRequest
     * @return
     */
    @DeleteMapping("/comments/{commentId}")
    public ResponseVO deleteComment(@PathVariable String commentId, HttpServletRequest httpRequest) {
        String username = (String) httpRequest.getAttribute("username");
        if (username == null) {
            return ResponseVO.authFail();
        }

        log.info("댓글 삭제 요청: commentId: {}, username: {}", commentId, username);

        try {
            commentService.deleteComment(commentId, username);
            return ResponseVO.deleteOk();
        } catch (IllegalArgumentException e) {
            return ResponseVO.accessDenied();
        } catch (RuntimeException e) {
            return ResponseVO.error("댓글 삭제에 실패했습니다.");
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    //                                                  조회
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * 특정 게시글의 최상위 댓글 목록 조회(페이징)
     *
     * @param postId
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/posts/{postId}/comments")
    public ResponseVO getRootComments(@PathVariable String postId,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size,
                                      @RequestParam(defaultValue = "LATEST") String sort,
                                      HttpServletRequest httpRequest
    ) {
        log.info("최상위 댓글 목록 조회: postId: {}, page: {}, size: {}, sort: {}", postId, page, size, sort);

        String currentUsername = (String) httpRequest.getAttribute("username");

        PageResponse<CommentResponse> comments = commentService.getRootComments(
                postId, page, size, sort, currentUsername);
        return ResponseVO.ok(comments);
    }

    /**
     * 특정 댓글의 대댓글 목록 조회 (페이징)
     *
     * @param postId
     * @param commentId
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/posts/{postId}/comments/{commentId}/replies")
    public ResponseVO getReplies(@PathVariable String postId,
                                 @PathVariable String commentId,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size,
                                 HttpServletRequest httpRequest
    ) {
        log.info("대댓글 목록 조회: postId: {}, commentId: {}, page: {}, size: {}", postId, commentId, page, size);

        String currentUsername = (String) httpRequest.getAttribute("username");

        PageResponse<CommentResponse> replies = commentService.getReplies(
                postId, commentId, page, size, currentUsername);
        return ResponseVO.ok(replies);
    }
}
