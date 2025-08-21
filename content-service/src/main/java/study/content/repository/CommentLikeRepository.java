package study.content.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import study.content.entity.CommentLike;

import java.util.Optional;

public interface CommentLikeRepository extends MongoRepository<CommentLike, String> {

    // ======================= 핵심 기능 =======================

    /**
     * 특정 사용자가 특정 댓글에 좋아요했는지 확인
     *
     * @param commentId 댓글 ID
     * @param username  사용자명
     * @return 좋아요 여부
     */
    boolean existsByCommentIdAndUsername(String commentId, String username);

    /**
     * 특정 사용자가 특정 댓글에 한 좋아요 조회
     * 좋아요 삭제 시 사용
     *
     * @param commentId 댓글 ID
     * @param username  사용자명
     * @return 좋아요 객체
     */
    Optional<CommentLike> findByCommentIdAndUsername(String commentId, String username);

    /**
     * 특정 댓글의 총 좋아요 개수
     *
     * @param commentId 댓글 ID
     * @return 좋아요 개수
     */
    long countByCommentId(String commentId);

    // ======================= 관리용 =======================

    /**
     * 특정 사용자가 특정 댓글에 한 좋아요 삭제
     *
     * @param commentId 댓글 ID
     * @param username  사용자명
     * @return 삭제된 좋아요 개수
     */
    long deleteByCommentIdAndUsername(String commentId, String username);

    /**
     * 특정 사용자가 누른 모든 좋아요 조회(마이페이지)
     *
     * @param username 사용자명
     * @param pageable 페이징 정보
     * @return 사용자의 좋아요 목록
     */
    Page<CommentLike> findByUsernameOrderByCreatedAtDesc(String username, Pageable pageable);

    /**
     * 특정 댓글의 모든 좋아요 삭제
     * 댓글 삭제 시 연관된 좋아요도 함께 삭제
     *
     * @param commentId 댓글 ID
     * @return 삭제된 좋아요 개수
     */
    long deleteByCommentId(String commentId);

    /**
     * 특정 사용자의 모든 좋아요 삭제
     * 회원 탈퇴 시 연관된 좋아요 정리
     *
     * @param username 사용자명
     * @return 삭제된 좋아요 개수
     */
    long deleteByUsername(String username);
}
