package study.content.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.common.lib.exception.BaseException;
import study.common.lib.exception.ErrorCode;
import study.common.lib.response.PageResponse;
import study.common.lib.util.BasePagingUtil;
import study.common.lib.util.StringUtil;
import study.content.common.enums.CommentSortType;
import study.content.dto.comment.CommentRequest;
import study.content.dto.comment.CommentResponse;
import study.content.dto.comment.CommentUpdateRequest;
import study.content.entity.Comment;
import study.content.repository.CommentRepository;
import study.content.repository.PostRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    // -----------------------------------------------------------------------------------------------------------------
    //                                                  생성/수정/삭제
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * 댓글 생성
     *
     * @param request
     * @param author
     * @return
     */
    @Transactional
    public CommentResponse createComment(CommentRequest request, String author) {
        log.info("Creating comment for post: {} by author: {}", request.getPostId(), author);

        // 1. 게시글 존재 여부 확인
        validatePostExists(request.getPostId());

        // 2. 대댓글인 경우 부모 댓글 존재 여부 확인
        if (!StringUtil.isEmpty(request.getParentCommentId())) {
            validateParentCommentExists(request.getPostId(), request.getParentCommentId());
        }

        Comment comment = Comment.builder()
                .postId(request.getPostId())
                .author(author)
                .content(request.getContent())
                .parentCommentId(request.getParentCommentId())
                .build();

        Comment savedComment = commentRepository.save(comment);
        log.info("Comment created with id: {}", savedComment.getId());

        return CommentResponse.form(savedComment);
    }

    /**
     * 댓글 수정
     *
     * @param commentId
     * @param request
     * @param author
     * @return
     */
    @Transactional
    public CommentResponse updateComment(String commentId, CommentUpdateRequest request, String author) {
        log.info("Updating comment with id: {} by author: {}", commentId, author);

        // 1. ID로 활성 댓글 조회
        Comment comment = findActiveCommentById(commentId);

        // 2. 권한 검증
        validateAuthor(comment, author);

        // 3. 수정 및 저장
        comment.updateContent(request.getContent());
        Comment updatedComment = commentRepository.save(comment);

        log.info("Comment updated with id: {}", updatedComment.getId());
        return CommentResponse.form(updatedComment);
    }

    /**
     * 댓글 삭제 (Soft Delete)
     *
     * @param commentId
     * @param author
     */
    @Transactional
    public void deleteComment(String commentId, String author) {
        log.info("Deleting comment id: {} by author: {}", commentId, author);

        // 1. ID로 활성 댓글 조회
        Comment comment = findActiveCommentById(commentId);

        // 2. 권한 검증
        validateAuthor(comment, author);

        // 3. 삭제 및 저장
        comment.delete();
        commentRepository.save(comment);

        log.info("Comment soft deleted - id: {}", commentId);
    }

    // -----------------------------------------------------------------------------------------------------------------
    //                                                  조회 (BasePagingUtil 활용)
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * 특정 게시글의 최상위 댓글 목록 조회 (페이징, 정렬)
     *
     * @param postId
     * @param page
     * @param size
     * @return
     */
    public PageResponse<CommentResponse> getRootComments(String postId, int page, int size, String sort) {
        log.info("Fetching root comments for post: {} - page: {}, size: {}, sort: {}",
                postId, page, size, sort);

        // 게시글 존재 여부 확인
        validatePostExists(postId);

        // 댓글 정렬 타입 변환
        CommentSortType sortType = CommentSortType.fromString(sort);

        return BasePagingUtil.createPageResponse(
                page, size, sortType.toMongoSort(),
                pageable -> commentRepository.findRootCommentByPostId(postId, pageable),
                CommentResponse::form
        );
    }

    /**
     * 특정 댓글의 대댓글 미리보기 (처음 3개만 보여주기)
     *
     * @param postId
     * @param parentCommentId
     * @return
     */
    public List<CommentResponse> getReplyPreview(String postId, String parentCommentId) {
        log.info("Fetching reply preview for post: {} - parentCommentId: {}", postId, parentCommentId);

        // 게시글 존재 및 부모 댓글 존재 확인
        validateParentCommentExists(postId, parentCommentId);

        List<Comment> replies = commentRepository.findTop3RepliesByPostIdAndParentId(postId, parentCommentId);
        return replies.stream()
                .map(CommentResponse::form)
                .toList();
    }

    /**
     * 특정 댓글의 대댓글 목록 조회 (페이징)
     *
     * @param postId
     * @param parentCommentId
     * @param page
     * @param size
     * @return
     */
    public PageResponse<CommentResponse> getReplies(String postId, String parentCommentId, int page, int size) {
        log.info("Fetching replies for post: {} - parentCommentId: {}, page: {}, size: {}",
                postId, parentCommentId, page, size);

        // 게시글 존재 및 부모 댓글 존재 확인
        validateParentCommentExists(postId, parentCommentId);

        return BasePagingUtil.createUnsortedPageResponse(
                page, size,
                pageable -> commentRepository.findRepliesByParentId(postId, parentCommentId, pageable),
                CommentResponse::form
        );
    }

    // -----------------------------------------------------------------------------------------------------------------
    //                                                 기타 조회용
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * 마이페이지
     * 작성자별 댓글 조회
     *
     * @param author
     * @param page
     * @param size
     * @return
     */
    public PageResponse<CommentResponse> getCommentsByAuthor(String author, int page, int size, String sort) {
        log.info("Fetching comments by author: {} - page: {}, size: {}, sort: {}",
                author, page, size, sort);

        CommentSortType sortType = CommentSortType.fromString(sort);

        return BasePagingUtil.createPageResponse(
                page, size, sortType.toMongoSort(),
                pageable -> commentRepository.findByAuthor(author, pageable),
                CommentResponse::form
        );
    }

    /**
     * 신고페이지
     * 특정 게시글의 모든 댓글 조회 (관리자용 - 삭제된 것 포함)
     *
     * @param postId
     * @return
     */
    public List<CommentResponse> getAllCommentsForAdmin(String postId) {
        log.info("Fetching all comments for post: {}", postId);

        // 게시글 존재 여부 확인
        validatePostExists(postId);

        List<Comment> allComments = commentRepository.findAllCommentsByPostIdIncludingDeleted(postId);
        return allComments.stream()
                .map(CommentResponse::form)
                .toList();
    }

    // -----------------------------------------------------------------------------------------------------------------
    //                                                  통계
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * 특정 게시글의 모든 댓글 개수 조회
     *
     * @param postId
     * @return
     */
    public long getCommentStats(String postId) {
        log.info("Fetching comment stats for post: {}", postId);

        // 게시글 존재 여부 확인
        validatePostExists(postId);

        return commentRepository.countByPostId(postId);
    }

    /**
     * 특정 게시글 + 특정 댓글의 대댓글 개수 조회
     *
     * @param postId
     * @param parentCommentId
     * @return
     */
    public long getReplyCount(String postId, String parentCommentId) {
        log.info("Fetching reply count for post: {} - parentCommentId: {}", postId, parentCommentId);

        // 게시글 존재 및 부모 댓글 존재 확인
        validateParentCommentExists(postId, parentCommentId);

        return commentRepository.countRepliesByPostIdAndParentId(postId, parentCommentId);
    }

    // -----------------------------------------------------------------------------------------------------------------
    //                                             프라이빗 헬퍼 메서드
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * 게시글 존재 여부 확인
     *
     * @param postId
     */
    private void validatePostExists(String postId) {
        if (postRepository.findActivePostById(postId)
                .isEmpty()) {
            throw new BaseException(ErrorCode.POST_NOT_FOUND);
        }
    }

    /**
     * 부모 댓글 존재 여부 및 게시글 일치 확인
     *
     * @param postId
     * @param parentCommentId
     */
    private void validateParentCommentExists(String postId, String parentCommentId) {
        Comment parentComment = findActiveCommentById(parentCommentId);

        if (!parentComment.getPostId()
                .equals(postId)) {
            throw new BaseException(ErrorCode.COMMENT_ACCESS_DENIED, "해당 게시글의 댓글이 아닙니다");
        }

        if (parentComment.isReply()) {
            throw new BaseException(ErrorCode.INVALID_REQUEST, "대댓글에는 답글을 달 수 없습니다");
        }
    }

    /**
     * 활성 댓글 조회 (공통 로직)
     *
     * @param commentId
     * @return
     */
    private Comment findActiveCommentById(String commentId) {
        return commentRepository.findActiveCommentById(commentId)
                .orElseThrow(() -> new BaseException(ErrorCode.COMMENT_NOT_FOUND));
    }

    /**
     * 작성자 권한 검증
     *
     * @param comment
     * @param author
     */
    private void validateAuthor(Comment comment, String author) {
        if (!comment.isAuthor(author)) {
            throw new BaseException(ErrorCode.COMMENT_ACCESS_DENIED);
        }
    }
}
