package study.content.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import study.content.entity.Like;
import study.content.entity.Like.TargetType;

@Repository
public interface LikeRepository extends MongoRepository<Like, String> {

    /**
     * 특정 사용자가 특정 대상에 좋아요 했는지 확인
     *
     * @param targetId   대상 ID (게시글 or 댓글)
     * @param targetType 대상 타입 (POST or COMMENT)
     * @param username   사용자명
     * @return 좋아요 여부
     */
    boolean existsByTargetIdAndTargetTypeAndUsername(String targetId, TargetType targetType, String username);

    /**
     * 특정 대상의 총 좋아요 개수
     *
     * @param targetId   대상 ID
     * @param targetType 대상 타입
     * @return 좋아요 개수
     */
    long countByTargetIdAndTargetType(String targetId, TargetType targetType);

    /**
     * 특정 사용자가 특정 대상에 한 좋아요 삭제 (좋아요 취소)
     *
     * @param targetId   대상 ID
     * @param targetType 대상 타입
     * @param username   사용자명
     * @return 삭제된 좋아요 개수 (0 or 1)
     */
    long deleteByTargetIdAndTargetTypeAndUsername(String targetId, TargetType targetType, String username);

    /**
     * 특정 대상의 모든 좋아요 삭제
     * 게시글/댓글 삭제 시 연관된 좋아요도 함께 삭제
     *
     * @param targetId   대상 ID
     * @param targetType 대상 타입
     * @return 삭제된 좋아요 개수
     */
    long deleteByTargetIdAndTargetType(String targetId, TargetType targetType);

    /**
     * 특정 사용자의 모든 게시글 좋아요 삭제
     * 회원 탈퇴 시 연관된 좋아요 정리용
     *
     * @param username 사용자명
     * @return 삭제된 좋아요 개수
     */
    // TODO 회원탈퇴 생성 후 추가
    long deleteByUsername(String username);
}
