package study.content.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.common.lib.exception.BaseException;
import study.common.lib.exception.ErrorCode;
import study.content.dto.comment.LikeResponse;
import study.content.entity.PostLike;
import study.content.repository.PostLikeRepository;
import study.content.repository.PostRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * 게시글 좋아요 토글 + 결과 정보 반환
     *
     * @param postId   게시글 ID
     * @param username 사용자명
     * @return 토글 후 좋아요 상태 + 개수 정보
     */
    @Transactional
    public LikeResponse togglePostLikeAndGetInfo(String postId, String username) {
        log.info("게시글 좋아요 토글 with info - postId: {}, username: {}", postId, username);

        // 1. 기존 토글 로직 재사용
        boolean isLiked = togglePostLike(postId, username);

        // 2. 토글 후 최신 좋아요 개수 조정
        long likeCount = getPostLikeCount(postId);

        // 3. 응답 객체 생성
        LikeResponse response = LikeResponse.builder()
                .id(postId)
                .likeCount(likeCount)
                .isLikedByCurrentUser(isLiked)
                .build();

        log.info("게시글 좋아요 토글 완료 - postId: {}, isLiked: {}, likeCount: {}",
                postId, isLiked, likeCount);

        return response;
    }

    /**
     * 여러 게시글의 좋아요 개수 일괄 조회
     *
     * @param postIds 게시글 ID 목록
     * @return Map<postid, likeCount>
     */
    public Map<String, Long> getBulkLikeCounts(List<String> postIds) {
        log.info("Bulk fetching like counts for {} posts", postIds.size());

        Map<String, Long> result = new HashMap<>();

        for (String postId : postIds) {
            try {
                // 각 게시글의 좋아요 개수 조회
                long likeCount = postLikeRepository.countByPostId(postId);
                result.put(postId, likeCount);
            } catch (Exception e) {
                log.warn("Failed to get like count for post: {}, error: {}", postId, e.getMessage());

                // 실패한 경우 0으로 설정
                result.put(postId, 0L);
            }
        }
        log.info("Bulk like counts fetched successfully: {} results", result.size());
        return result;
    }

    /**
     * 게시글 좋아요 정보 조회
     *
     * @param postId   게시글 ID
     * @param username 사용자명
     * @return 게시글별 좋아요 정보
     */
    public LikeResponse getPostLikeInfo(String postId, String username) {
        log.info("게시글 좋아요 정보 조회 - postId: {}, username: {}", postId, username);

        // 1. 게시글 존재 확인
        validatePostExists(postId);

        // 2. 좋아요 개수 조회
        long likeCount = getPostLikeCount(postId);

        // 3. 현재 사용자 좋아여 여부 (로그인한 경우만)
        boolean isLiked = false;
        if (username != null) {
            isLiked = postLikeRepository.existsByPostIdAndUsername(postId, username);
        }

        // 4. 응답 객체 생성
        LikeResponse response = LikeResponse.builder()
                .id(postId)
                .likeCount(likeCount)
                .isLikedByCurrentUser(isLiked)
                .build();

        log.info("좋아요 정보 조회 완료 - postId: {}, likeCount: {}, isLiked: {}",
                postId, likeCount, isLiked);

        return response;
    }
}
