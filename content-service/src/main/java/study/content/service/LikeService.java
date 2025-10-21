package study.content.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.common.lib.exception.BaseException;
import study.common.lib.exception.ErrorCode;
import study.content.dto.comment.LikeResponse;
import study.content.entity.Like;
import study.content.entity.Like.TargetType;
import study.content.repository.CommentRepository;
import study.content.repository.LikeRepository;
import study.content.repository.PostRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikeService {
    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    /**
     * 단순 게시글 좋아요 토글 (생성/삭제)
     * 이미 좋아요가 있으면 삭제, 없으면 생성
     *
     * @param targetId   대상 ID (게시글 또는 댓글)
     * @param targetType 대상 타입 (POST, COMMENT)
     * @param username   사용자명
     * @return 좋아요 상태(true: 생성 / false: 삭제)
     */
    @Transactional
    public boolean toggleLike(String targetId, TargetType targetType, String username) {
        log.debug("좋아요 토글 - targetId: {}, targetType: {}, username: {}",
                targetId, targetType, username);

        // 1. 대상 존재 여부 확인
        validateTargetExists(targetId, targetType);

        // 2. 이미 좋아요가 있는지 확인
        boolean alreadyLiked = likeRepository.existsByTargetIdAndTargetTypeAndUsername(targetId, targetType, username);

        if (alreadyLiked) {
            // 이미 좋아요가 있으므로 삭제
            long deletedCount = likeRepository.deleteByTargetIdAndTargetTypeAndUsername(targetId, targetType, username);
            log.info("좋아요 삭제 - targetId: {}, targetType: {}, username: {}, deletedCount: {}",
                    targetId, targetType, username, deletedCount);
            return false;
        } else {
            Like newLike = Like.create(targetId, targetType, username);
            Like savedLike = likeRepository.save(newLike);

            log.info("좋아요 생성 - id: {}, targetId: {}, targetType: {}, username: {}",
                    savedLike.getId(), targetId, targetType, username);
            return true;
        }
    }

    /**
     * 좋아요 토글 + 결과 정보 반환
     *
     * @param targetId   대상 ID
     * @param targetType 대상 타입
     * @param username   사용자명
     * @return 좋아요여부 + 좋아요개수
     */
    @Transactional
    public LikeResponse toggleLikeAndGetInfo(String targetId, TargetType targetType, String username) {
        log.info("좋아요 토글 및 정보 조회 - targetId: {}, targetType: {}, username: {}",
                targetId, targetType, username);

        // 1. 좋아요 토글
        boolean isLiked = toggleLike(targetId, targetType, username);

        // 2. 좋아요 개수 조회
        long likeCount = getLikeCount(targetId, targetType);

        // 3. 응답 생성
        LikeResponse response = LikeResponse.builder()
                .id(targetId)
                .likeCount(likeCount)
                .isLikedByCurrentUser(isLiked)
                .build();

        log.info("좋아요 토글 완료 - targetId: {}, targetType: {}, isLiked: {}, likeCount: {}",
                targetId, targetType, isLiked, likeCount);

        return response;
    }

    /**
     * 특정 게시글의 좋아요 개수 조회
     *
     * @param targetId   대상 ID
     * @param targetType 대상 타입
     * @return 좋아요 개수
     */
    public long getLikeCount(String targetId, TargetType targetType) {
        long likeCount = likeRepository.countByTargetIdAndTargetType(targetId, targetType);
        log.debug("좋아요 개수 조회 - targetId: {}, targetType: {}, count: {}",
                targetId, targetType, likeCount);
        return likeCount;
    }

    /**
     * 여러 대상의 좋아요 개수 일괄 조회
     * 게시판 목록에서 각 게시판별 좋아요 개수 구하기
     *
     * @param targetIds  대상 ID 목록
     * @param targetType 대상 타입
     * @return 대상 ID를 키로 하는 좋아요 개수 맵
     */
    public Map<String, Long> getBulkLikeCount(List<String> targetIds, TargetType targetType) {
        log.debug("좋아요 일괄 조회 - targets: {}개, type: {}",
                targetIds.size(), targetType);

        Map<String, Long> result = new HashMap<>();

        // 각 대상의 좋아요 개수 조회
        for (String targetId : targetIds) {
            try {
                long likeCount = likeRepository.countByTargetIdAndTargetType(targetId, targetType);
                result.put(targetId, likeCount);
            } catch (Exception e) {
                log.warn("Failed to get like count for target: {}, error: {}", targetId, e.getMessage());
                result.put(targetId, 0L); // 실패한 경우 0으로 설정
            }
        }

        log.debug("좋아요 일괄 조회 완료 - 결과: {}개", result.size());
        return result;
    }

    /**
     * 좋아요 정보 조회 (개수 + 현재 사용자 좋아요 여부)
     * 상세화면에서 조회하는 용도
     *
     * @param targetId   대상 ID
     * @param targetType 대상 타입
     * @param username   현재 사용자명(null 가능)
     * @return 좋아요 정보
     */
    public LikeResponse getLikeInfo(String targetId, TargetType targetType, String username) {
        log.debug("좋아요 정보 조회 - targetId: {}, targetType: {}, user: {}",
                targetId, targetType, username != null ? username : "비로그인");

        // 1. 대상 존재 확인
        validateTargetExists(targetId, targetType);

        // 2. 좋아요 개수 조회
        long likeCount = getLikeCount(targetId, targetType);

        // 3. 현재 사용자 좋아요 여부 (로그인한 경우만)
        boolean isLiked = false;
        if (username != null) {
            isLiked = likeRepository.existsByTargetIdAndTargetTypeAndUsername(targetId, targetType, username);
        }

        // 4. 응답 객체 생성
        LikeResponse response = LikeResponse.builder()
                .id(targetId)
                .likeCount(likeCount)
                .isLikedByCurrentUser(isLiked)
                .build();

        log.debug("좋아요 정보 조회 완료 - likeCount: {}, isLiked: {}",
                likeCount, isLiked);

        return response;
    }

    // ======================= Private 헬퍼 메서드 =======================

    /**
     * 대상(게시글/댓글) 존재 여부 확인
     *
     * @param targetId   대상 ID
     * @param targetType 대상 타입
     */
    private void validateTargetExists(String targetId, TargetType targetType) {
        if (targetType == TargetType.POST) {
            postRepository.findActivePostById(targetId)
                    .orElseThrow(() -> {
                        log.warn("게시글 없음 - postId: {}", targetId);
                        return new BaseException(ErrorCode.POST_NOT_FOUND);
                    });
        } else if (targetType == TargetType.COMMENT) {
            commentRepository.findActiveCommentById(targetId)
                    .orElseThrow(() -> {
                        log.warn("댓글 없음 - commentId: {}", targetId);
                        return new BaseException(ErrorCode.COMMENT_NOT_FOUND);
                    });
        }
    }
}
