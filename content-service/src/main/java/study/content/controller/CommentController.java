package study.content.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import study.common.lib.exception.BaseException;
import study.common.lib.exception.ErrorCode;
import study.common.lib.response.PageResponse;
import study.common.lib.response.ResponseVO;
import study.content.dto.comment.CommentRequest;
import study.content.dto.comment.CommentResponse;
import study.content.dto.comment.CommentUpdateRequest;
import study.content.service.CommentService;

import javax.sql.rowset.BaseRowSet;
import java.util.List;

/**
 * 댓글 관련 API Controller
 * 댓글 및 대댓글 CRUD 기능 제공
 * 최상위 댓글과 대댓글 조회 기능 포함
 */
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
     * 댓글 생성 (댓글 또는 대댓글)
     * parentCommentId가 없으면 최상위 댓글, 있으면 대댓글로 생성
     *
     * @param postId
     * @param request
     * @param httpRequest
     * @return
     */
    @PostMapping("/posts/{postId}/comments")
    public ResponseVO<CommentResponse> createComment(@PathVariable String postId,
                                                     @Valid @RequestBody CommentRequest request,
                                                     HttpServletRequest httpRequest) {
        String username = extractUsername(httpRequest);

        // postId 일치 확인
        validatePostId(postId, request.getPostId());

        log.info("댓글 생성 요청 - postId: {}, author: {}, isReply: {}",
                postId, username, request.getParentCommentId() != null);

        CommentResponse comment = commentService.createComment(request, username);
        return ResponseVO.saveOk(comment);
    }

    /**
     * 댓글 수정
     * 작성자만 수정 가능
     *
     * @param commentId   댓글 ID
     * @param request     수정 요청
     * @param httpRequest HTTP 요청(username 포함)
     * @return 수정된 댓글 정보
     */
    @PutMapping("/comments/{commentId}")
    public ResponseVO<CommentResponse> updateComment(@PathVariable String commentId,
                                                     @Valid @RequestBody CommentUpdateRequest request,
                                                     HttpServletRequest httpRequest) {
        String username = extractUsername(httpRequest);

        log.info("댓글 수정 요청: commentId: {}, username: {}", commentId, username);
        CommentResponse comment = commentService.updateComment(commentId, request, username);
        return ResponseVO.updateOk(comment);
    }

    /**
     * 댓글 삭제 (소프트 삭제)
     * 작성자만 삭제 가능
     * 대댓글이 있는 경우 '삭제된 댓글입니다' 상태로 표시
     *
     * @param commentId   댓글 ID
     * @param httpRequest HTTP 요청 (username 포함)
     * @return 삭제 완료 응답
     */
    @DeleteMapping("/comments/{commentId}")
    public ResponseVO<Void> deleteComment(@PathVariable String commentId, HttpServletRequest httpRequest) {
        String username = extractUsername(httpRequest);

        log.info("댓글 삭제 요청: commentId: {}, username: {}", commentId, username);

        commentService.deleteComment(commentId, username);
        return ResponseVO.deleteOk();
    }

    // -----------------------------------------------------------------------------------------------------------------
    //                                                  조회
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * 특정 게시글의 최상위 댓글 목록 조회 (페이징)
     *
     * @param postId      게시글 ID
     * @param page        페이지 번호(0부터 시작)
     * @param size        페이지 크기
     * @param sort        정렬 방식 (LATEST, OLDEST)
     * @param httpRequest HTTP 요청(username이 있으면 작성자 여부 확인)
     * @return 최상위 댓글 목록
     */
    @GetMapping("/posts/{postId}/comments")
    public ResponseVO<PageResponse<CommentResponse>> getRootComments(@PathVariable String postId,
                                                                     @RequestParam(defaultValue = "0") int page,
                                                                     @RequestParam(defaultValue = "10") int size,
                                                                     @RequestParam(defaultValue = "LATEST") String sort,
                                                                     HttpServletRequest httpRequest
    ) {
        String currentUsername = (String) httpRequest.getAttribute("username");

        log.debug("최상위 댓글 목록 조회 - postId: {}, page: {}, size: {}, sort: {}",
                postId, page, size, sort);

        PageResponse<CommentResponse> comments = commentService.getRootComments(
                postId, page, size, sort, currentUsername);
        return ResponseVO.ok(comments);
    }

    /**
     * 특정 댓글의 대댓글 목록 조회 (페이징)
     *
     * @param postId      게시글 ID
     * @param commentId   부모 댓글 ID
     * @param page        페이지 번호(0부터 시작)
     * @param size        페이지 크기
     * @param httpRequest HTTP 요청
     * @return 대댓글 목록
     */
    @GetMapping("/posts/{postId}/comments/{commentId}/replies")
    public ResponseVO<PageResponse<CommentResponse>> getReplies(@PathVariable String postId,
                                                                @PathVariable String commentId,
                                                                @RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "10") int size,
                                                                HttpServletRequest httpRequest
    ) {
        String currentUsername = (String) httpRequest.getAttribute("username");

        log.debug("대댓글 목록 조회 - postId: {}, commentId: {}, page: {}, size: {}",
                postId, commentId, page, size);

        PageResponse<CommentResponse> replies = commentService.getReplies(
                postId, commentId, page, size, currentUsername);
        return ResponseVO.ok(replies);
    }

    // ======================= Private 헬퍼 메서드 =======================

    /**
     * HTTP 요청에서 username 추출 및 검증
     *
     * @param request HTTP 요청
     * @return username
     */
    private String extractUsername(HttpServletRequest request) {
        String username = (String) request.getAttribute("username");

        if (username == null) {
            log.warn("인증되지 않은 댓글 요청");
            throw new study.common.lib.exception.BaseException(
                    study.common.lib.exception.ErrorCode.UNAUTHORIZED
            );
        }

        return username;
    }

    /**
     * URL의 postId와 요청 body의 postId 일치 여부 검증
     *
     * @param urlPostId  URL 경로의 postId
     * @param bodyPostId 요청 body의 postId
     */
    private void validatePostId(String urlPostId, String bodyPostId) {
        if (!urlPostId.equals(bodyPostId)) {
            log.warn("postId 불일치 - URL: {}, Body: {}", urlPostId, bodyPostId);
            throw new BaseException(
                    ErrorCode.INVALID_REQUEST,
                    "URL의 게시글 ID와 요청 데이터가 일치하지 않습니다."
            );
        }
    }
}
