package study.content.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.common.lib.exception.BaseException;
import study.common.lib.exception.ErrorCode;
import study.content.entity.CommentLike;
import study.content.repository.CommentLikeRepository;
import study.content.repository.CommentRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentLikeService {

    private final CommentLikeRepository commentLikeRepository;
    private final CommentRepository commentRepository;

    /**
     * 댓글 좋아요 토글 (생성/삭제)
     * 이미 좋아요가 있으면 삭제, 없으면 생성
     *
     * @param commentId 댓글 ID
     * @param username  사용자명
     * @return 좋아요 상태 (true: 생성 / false: 삭제)
     */
    @Transactional
    public boolean toggleCommentLike(String commentId, String username) {
        log.info("Toggling comment like - commentId: {}, username: {}", commentId, username);

        // 1. 댓글 존재 여부 확인
        validateCommentExists(commentId);

        // 2. 이미 좋아요가 있는지 확인
        boolean alreadyLiked = commentLikeRepository.existsByCommentIdAndUsername(commentId, username);

        if (alreadyLiked) {
            // 이미 좋아요가 있으므로 삭제
            long deletedCount = commentLikeRepository.deleteByCommentIdAndUsername(commentId, username);
            log.info("Comment like removed - commentId: {}, username: {}, deletedCount: {}",
                    commentId, username, deletedCount);
            return false;
        } else {
            // 좋아요가 없으므로 생성
            CommentLike newLike = CommentLike.create(commentId, username);
            CommentLike savedLike = commentLikeRepository.save(newLike);
            log.info("Comment like created - id: {}, commentId: {}, username: {}",
                    savedLike.getId(), commentId, username);
            return true;
        }
    }

    /**
     * 특정 댓글의 좋아요 개수 조회
     *
     * @param commentId 댓글 ID
     * @return 좋아요 개수
     */
    public long getCommentLikeCount(String commentId) {
        log.debug("Getting like count for comment: {}", commentId);

        validateCommentExists(commentId);

        long likeCount = commentLikeRepository.countByCommentId(commentId);
        log.debug("Comment like count - commentId: {}, count: {}", commentId, likeCount);

        return likeCount;
    }

    /**
     * 댓글 존재 여부
     *
     * @param commentId 댓글 ID
     */
    private void validateCommentExists(String commentId) {
        commentRepository.findActiveCommentById(commentId)
                .orElseThrow(() -> {
                    log.warn("활성 댓글을 찾을 수 없습니다. commentId: {}", commentId);
                    return new BaseException(ErrorCode.COMMENT_NOT_FOUND);
                });
    }
}
