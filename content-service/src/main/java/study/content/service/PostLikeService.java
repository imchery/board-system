package study.content.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.common.lib.exception.BaseException;
import study.common.lib.exception.ErrorCode;
import study.content.entity.PostLike;
import study.content.repository.PostLikeRepository;
import study.content.repository.PostRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;

    /**
     * 게시글 좋아요 토글 (생성/삭제)
     * 이미 좋아요가 있으면 삭제, 없으면 생성
     *
     * @param postId   게시글 ID
     * @param username 사용자명
     * @return 좋아요 상태 (true: 생성 / false: 삭제)
     */
    @Transactional
    public boolean togglePostLike(String postId, String username) {
        log.info("Toggling post like - postId: {}, username: {}", postId, username);

        // 1. 게시글 존재 여부 확인
        validatePostExists(postId);

        // 2. 이미 좋아요가 있는지 확인
        boolean alreadyLiked = postLikeRepository.existsByPostIdAndUsername(postId, username);

        if (alreadyLiked) {
            // 이미 좋아요가 있으므로 삭제
            long deletedCount = postLikeRepository.deleteByPostIdAndUsername(postId, username);
            log.info("Post like removed - postId: {}, username: {}, deletedCount: {}",
                    postId, username, deletedCount);
            return false;
        } else {
            PostLike newLike = PostLike.create(postId, username);
            PostLike savedLike = postLikeRepository.save(newLike);
            log.info("Post like created - id: {}, postId: {}, username: {}",
                    savedLike.getId(), postId, username);
            return true;
        }
    }

    /**
     * 특정 게시글의 좋아요 개수 조회
     *
     * @param postId 게시글 ID
     * @return 좋아요 개수
     */
    public long getPostLikeCount(String postId) {
        log.debug("Getting like count for post: {}", postId);

        validatePostExists(postId);

        long likeCount = postLikeRepository.countByPostId(postId);
        log.debug("Post like count - postId: {}, count: {}", postId, likeCount);

        return likeCount;
    }

    /**
     * 게시글 존재 여부 확인
     *
     * @param postId 게시글 ID
     */
    private void validatePostExists(String postId) {
        postRepository.findActivePostById(postId)
                .orElseThrow(() -> {
                    log.warn("활성 게시글을 찾을 수 없습니다. postId: {}", postId);
                    return new BaseException(ErrorCode.POST_NOT_FOUND);
                });
    }
}
