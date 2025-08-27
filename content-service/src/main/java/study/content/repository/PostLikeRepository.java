package study.content.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import study.content.entity.PostLike;

public interface PostLikeRepository extends MongoRepository<PostLike, String> {

    /**
     * 특정 사용자가 특정 게시글에 좋아요했는지 확인
     *
     * @param postId   게시글 ID
     * @param username 사용자명
     * @return 좋아요 여부
     */
    boolean existsByPostIdAndUsername(String postId, String username);

    /**
     * 특정 게시글의 총 좋아요 개수
     *
     * @param postId 게시글 ID
     * @return 좋아요 개수
     */
    long countByPostId(String postId);

    /**
     * 특정 사용자가 특정 게시글에 한 좋아요 삭제
     *
     * @param postId   게시글 ID
     * @param username 사용자명
     * @return 삭제된 좋아요 개수
     */
    long deleteByPostIdAndUsername(String postId, String username);

    /**
     * 특정 사용자가 누른 모든 좋아요 조회 (마이페이지용)
     *
     * @param username 사용자명
     * @param pageable 페이징 정보
     * @return 사용자의 게시글 좋아요 목록
     */
    // TODO 마이페이지 생성 후 추가
    Page<PostLike> findByUsernameOrderByCreatedAtDesc(String username, Pageable pageable);

    /**
     * 특정 게시글의 모든 좋아요 삭제
     * 게시글 삭제 시 연관된 좋아요도 함께 삭제
     *
     * @param postId 게시글 ID
     * @return 삭제된 좋아요 개수
     */
    long deleteByPostId(String postId);

    /**
     * 특정 사용자의 모든 게시글 좋아요 삭제
     * 회원 탈퇴 시 연관된 좋아요 정리용
     *
     * @param username 사용자명
     * @return 삭제된 좋아요 개수
     */
    // TODO 회원탈퇴 생성 후 추가
    long deleteByUsername(String username);

    /**
     * 특정 게시글에 좋아요한 사용자 목록 조회 (관리자용)
     *
     * @param postId   게시글 ID
     * @param pageable 페이징 정보
     * @return 좋아요한 사용자 목록
     */
    // TODO 관리자 페이지 생성 후 추가
    Page<PostLike> findByPostIdOrderByCreatedAtDesc(String postId, Pageable pageable);
}
