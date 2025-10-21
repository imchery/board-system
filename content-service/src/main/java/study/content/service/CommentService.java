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
import study.content.entity.Like;
import study.content.repository.CommentRepository;
import study.content.repository.LikeRepository;
import study.content.repository.PostRepository;

import java.util.List;

/**
 * 댓글 비즈니스 로직 처리 Service
 * 댓글 CRUD, 대댓글 관리, 좋아요 정보 조회 등의 비즈니스 로직 담당
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final LikeRepository likeRepository;

    // -----------------------------------------------------------------------------------------------------------------
    //                                                  생성/수정/삭제
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * 댓글 생성 (댓글 또는 대댓글)
     *
     * @param request 댓글 생성 요청
     * @param author  작성자
     * @return 생성된 댓글 정보
     */
    @Transactional
    public CommentResponse createComment(CommentRequest request, String author) {
        log.info("댓글 생성 - postId: {}, author: {}, isReply: {}",
                request.getPostId(), author, request.getParentCommentId() != null);

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
        log.info("댓글 생성 완료 - commentId: {}", savedComment.getId());

        return CommentResponse.from(savedComment);
    }

    /**
     * 댓글 수정
     * 작성자만 수정 가능
     *
     * @param commentId 댓글 ID
     * @param request   수정요청
     * @param author    요청자
     * @return 수정된 댓글 정보
     */
    @Transactional
    public CommentResponse updateComment(String commentId, CommentUpdateRequest request, String author) {
        log.info("댓글 수정 - commentId: {}, author: {}", commentId, author);

        // 1. ID로 활성 댓글 조회
        Comment comment = findActiveCommentById(commentId);

        // 2. 권한 검증
        validateAuthor(comment, author);

        // 3. 수정 및 저장
        comment.updateContent(request.getContent());
        Comment updatedComment = commentRepository.save(comment);

        log.info("댓글 수정 완료 - commentId: {}", updatedComment.getId());
        return CommentResponse.from(updatedComment);
    }

    /**
     * 댓글 삭제 (Soft Delete + 연관 데이터 정리)
     * 삭제 순서:
     * 부모 댓글인 경우: 모든 대댓글도 함께 삭제
     * 각 댓글의 좋아요 물리 삭제
     * 댓글 소프트 삭제
     *
     * @param commentId 댓글 ID
     * @param author    요청자
     */
    @Transactional
    public void deleteComment(String commentId, String author) {
        log.info("댓글 삭제 시작 - commentId: {}, author: {}", commentId, author);

        // 1. ID로 활성 댓글 조회
        Comment comment = findActiveCommentById(commentId);

        // 2. 권한 검증
        validateAuthor(comment, author);

        long totalLikesDeleted = 0;

        // 3. 부모 댓글인 경우 모든 대댓글 처리
        if (!comment.isReply()) {
            List<Comment> replies = commentRepository.findActiveRepliesByParentId(commentId);

            log.debug("삭제 대상 대댓글 수: {}", replies.size());

            for (Comment reply : replies) {
                // 각 대댓글의 좋아요 물리 삭제
                totalLikesDeleted += likeRepository.deleteByTargetIdAndTargetType(
                        reply.getId(),
                        Like.TargetType.COMMENT
                );
                reply.delete();
            }

            if (!replies.isEmpty()) {
                commentRepository.saveAll(replies);
            }

            log.debug("대댓글 처리 완료 - 댓글: {}개, 좋아요: {}개",
                    replies.size(), totalLikesDeleted);
        }

        // 4. 원본 댓글 좋아요 물리 삭제
        long commentLikesDeleted = likeRepository.deleteByTargetIdAndTargetType(commentId, Like.TargetType.COMMENT);
        totalLikesDeleted += commentLikesDeleted;

        // 5. 원본 댓글 soft delete
        comment.delete();
        commentRepository.save(comment);

        log.info("댓글 삭제 완료 - commentId: {}, 총 좋아요 삭제: {}개",
                commentId, totalLikesDeleted);
    }

    // -----------------------------------------------------------------------------------------------------------------
    //                                                  조회 (BasePagingUtil 활용)
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * 특정 게시글의 최상위 댓글 목록 조회 (페이징 + 정렬 + 좋아요 정보)
     *
     * @param postId          게시글 ID
     * @param page            페이지 번호
     * @param size            페이지 크기
     * @param sort            정렬 방식 (LATEST, OLDEST)
     * @param currentUsername 현재 사용자(null 가능)
     * @return 댓글 목록 (좋아요 정보 포함)
     */
    public PageResponse<CommentResponse> getRootComments(
            String postId, int page, int size, String sort, String currentUsername
    ) {
        log.debug("최상위 댓글 조회 - postId: {}, page: {}, size: {}, sort: {}, user: {}",
                postId, page, size, sort, currentUsername != null ? currentUsername : "비로그인");

        // 1. 게시글 존재 여부 확인
        validatePostExists(postId);

        // 2. 댓글 정렬 타입 변환
        CommentSortType sortType = CommentSortType.fromString(sort);

        // 3. 기본 댓글 목록 조회
        PageResponse<CommentResponse> basicComments = BasePagingUtil.createPageResponse(
                page, size, sortType.toMongoSort(),
                pageable -> commentRepository.findRootCommentByPostId(postId, pageable),
                CommentResponse::from
        );

        // 4. 각 댓글에 좋아요 정보 추가
        List<CommentResponse> commentsWithLikes = basicComments.getContent()
                .stream()
                .map(comment -> enrichCommentWithLikeInfo(comment, currentUsername))
                .toList();

        // 5. PageResponse 재구성
        return PageResponse.withNewContent(basicComments, commentsWithLikes);
    }

    /**
     * 특정 댓글의 대댓글 목록 조회 (페이징 + 좋아요 정보)
     *
     * @param postId          게시글 ID
     * @param parentCommentId 부모 댓글 ID
     * @param page            페이지 정보
     * @param size            페이지 크기
     * @param currentUsername 현재 사용자(null 가능)
     * @return 대댓글 목록 (쫗아요 정보 포함)
     */
    public PageResponse<CommentResponse> getReplies(
            String postId, String parentCommentId, int page, int size, String currentUsername) {

        log.debug("대댓글 조회 - postId: {}, parentId: {}, page: {}, size: {}, user: {}",
                postId, parentCommentId, page, size,
                currentUsername != null ? currentUsername : "비로그인");

        // 1. 게시글 존재 및 부모 댓글 존재 확인
        validateParentCommentExists(postId, parentCommentId);

        // 2. 기본 대댓글 목록 조회
        PageResponse<CommentResponse> basicReplies = BasePagingUtil.createUnsortedPageResponse(
                page, size,
                pageable -> commentRepository.findRepliesByParentId(postId, parentCommentId, pageable),
                CommentResponse::from
        );

        // 3. 각 댓글에 좋아요 정보 추가
        List<CommentResponse> repliesWithLikes = basicReplies.getContent()
                .stream()
                .map(comment -> enrichCommentWithLikeInfo(comment, currentUsername))
                .toList();

        // 4. PageResponse 재구성
        return PageResponse.withNewContent(basicReplies, repliesWithLikes);
    }

    // -----------------------------------------------------------------------------------------------------------------
    //                                             프라이빗 헬퍼 메서드
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * 게시글 존재 여부 확인
     *
     * @param postId 게시글 ID
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
     * @param postId          게시글 ID
     * @param parentCommentId 부모 댓글 ID
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
     * @param commentId 댓글 ID
     * @return 활성 상태의 댓글 Entity
     */
    private Comment findActiveCommentById(String commentId) {
        return commentRepository.findActiveCommentById(commentId)
                .orElseThrow(() -> new BaseException(ErrorCode.COMMENT_NOT_FOUND, "댓글을 찾을 수 없습니다. ID: " + commentId));
    }

    /**
     * 작성자 권한 검증 (공통 로직)
     *
     * @param comment 댓글 Entity
     * @param author  요청자
     */
    private void validateAuthor(Comment comment, String author) {
        if (!comment.isAuthor(author)) {
            log.warn("권한 없는 접근 시도 - commentId: {}, author: {}, requester: {}",
                    comment.getId(), comment.getAuthor(), author);
            throw new BaseException(ErrorCode.COMMENT_ACCESS_DENIED, "댓글에 대한 권한이 없습니다.");
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    //                                                헬퍼 메서드
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * 댓글에 좋아요 정보를 추가하는 헬퍼 메서드
     * 기존 CommentResponse에 좋아요 개수와 현재 사용자의 좋아요 여부 추가
     *
     * @param comment         기본 댓글 정보
     * @param currentUsername 현재 사용자명(null 가능)
     * @return 좋아요 정보가 포함된 댓글 응답
     */
    private CommentResponse enrichCommentWithLikeInfo(CommentResponse comment, String currentUsername) {

        // 1. 해당 댓글의 좋아요 개수 조회
        long likeCount = likeRepository.countByTargetIdAndTargetType(comment.getId(), Like.TargetType.COMMENT);

        // 2. 현재 사용자의 좋아요 여부 확인 (로그인한 경우만)
        boolean isLikedByCurrentUser = false;
        if (currentUsername != null) {
            isLikedByCurrentUser = likeRepository.existsByTargetIdAndTargetTypeAndUsername(comment.getId(), Like.TargetType.COMMENT, currentUsername);
        }

        // 3. 기존 CommentResponse에 좋아요 정보 추가
        comment.setLikeCount(likeCount);
        comment.setIsLikedByCurrentUser(isLikedByCurrentUser);
        return comment;
    }
}
