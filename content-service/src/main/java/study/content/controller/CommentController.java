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
                                      @RequestParam(defaultValue = "10") int size) {
        log.info("최상위 댓글 목록 조회: postId: {}, page: {}, size: {}", postId, page, size);

        PageResponse<CommentResponse> comments = commentService.getRootComments(postId, page, size);
        return ResponseVO.ok(comments);
    }

    /**
     * 특정 댓글의 대댓글 미리보기 (처음 3개)
     *
     * @param postId
     * @param commentId
     * @return
     */
    @GetMapping("/posts/{postId}/comments/{commentId}/preview")
    public ResponseVO getReplyPreview(@PathVariable String postId,
                                      @PathVariable String commentId) {
        log.info("대댓글 미리보기 조회: postId: {}, commentId: {}", postId, commentId);

        List<CommentResponse> replies = commentService.getReplyPreview(postId, commentId);
        return ResponseVO.ok(replies);
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
                                 @RequestParam(defaultValue = "10") int size) {
        log.info("대댓글 목록 조회: postId: {}, commentId: {}, page: {}, size: {}", postId, commentId, page, size);

        PageResponse<CommentResponse> replies = commentService.getReplies(postId, commentId, page, size);
        return ResponseVO.ok(replies);
    }

    // -----------------------------------------------------------------------------------------------------------------
    //                                                 기타 조회용
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * 작성자별 댓글 조회
     *
     * @param author
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/comments/author/{author}")
    public ResponseVO getCommentsByAuthor(@PathVariable String author,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size) {
        log.info("작성자별 댓글 조회: author: {}, page: {}, size: {}", author, page, size);
        PageResponse<CommentResponse> comments = commentService.getCommentsByAuthor(author, page, size);
        return ResponseVO.ok(comments);
    }

    /**
     * 특정 게시글의 모든 댓글 조회 (관리자용 - 삭제된 것 포함)
     * @param postId
     * @return
     */
    @GetMapping("/admin/posts/{postId}/comments")
    public ResponseVO getAllCommentsForAdmin(@PathVariable String postId) {
        log.info("관리자 댓글 조회: postId: {}", postId);

        // TODO 관리자 권한 체크 로직 추가 필요
        List<CommentResponse> comments = commentService.getAllCommentsForAdmin(postId);
        return ResponseVO.ok(comments);
    }

    // -----------------------------------------------------------------------------------------------------------------
    //                                                  통계
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * 특정 게시글의 댓글 통계 조회
     *
     * @param postId
     * @return
     */
    @GetMapping("/posts/{postId}/comments/stats")
    public ResponseVO getCommentStats(@PathVariable String postId) {
        log.info("댓글 통계 조회: postId: {}", postId);

        long commentStats = commentService.getCommentStats(postId);
        return ResponseVO.ok(commentStats);
    }

    /**
     * 특정 댓글의 대댓글 개수 조회
     *
     * @param postId
     * @param commentId
     * @return
     */
    @GetMapping("/posts/{postId}/comments/{commentId}/count")
    public ResponseVO getReplyCount(@PathVariable String postId,
                                    @PathVariable String commentId) {
        log.info("대댓글 개수 조회: postId: {}, commentId: {}", postId, commentId);
        long replyCount = commentService.getReplyCount(postId, commentId);
        return ResponseVO.ok(replyCount);
    }
}
