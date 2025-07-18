package study.content.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import study.content.entity.Comment;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends MongoRepository<Comment, String> {

    // ======================= 기본 댓글 조회 =======================

    /**
     * 특정 게시글의 활성 댓글 조회 (페이징)
     * 최상위 댓글만 조회 (대댓글 제외)
     *
     * @param postId
     * @param pageable
     * @return
     */
    @Query("{'postId': ?0, 'status': 'ACTIVE', 'parentCommentId': null}")
    Page<Comment> findRootCommentByPostId(String postId, Pageable pageable);

    /**
     * 특정 게시글의 모든 활성 댓글 조회 (대댓글 포함)
     */
    @Query("{'postId':  ?0, 'status': 'ACTIVE'}")
    Page<Comment> findAllCommentsByPostId(String postId, Pageable pageable);

    /**
     * 특정 게시글의 특정 댓글에 달린 대댓글들 조회(페이징)
     */
    @Query("{'postId': ?0, 'parentCommentId': ?1, 'status': 'ACTIVE'}")
    Page<Comment> findRepliesByParentId(String postId, String parentCommentId, Pageable pageable);

    /**
     * 특정 게시글의 특정 댓글에 달린 대댓글들 조회(전체 or 미리보기용)
     * 처음 몇 개만 보여주고 '답글 더 보기' 구현할 때 사용
     *
     * @param postId
     * @param parentCommentId
     * @return
     */
    @Query("{'postId': ?0, 'parentCommentId': ?1, 'status': 'ACTIVE'}")
    List<Comment> findTop3RepliesByPostIdAndParentId(String postId, String parentCommentId);

    // ======================= 댓글 개별 조회 =======================

    /**
     * ID로 활성 댓글 조회
     *
     * @param id
     * @return
     */
    @Query("{'_id': ?0, 'status':  'ACTIVE'}")
    Optional<Comment> findActiveCommentById(String id);

    // ======================= 작성자별 조회 =======================

    /**
     * 작성자별 댓글 조회 (활성상태만)
     *
     * @param author
     * @param pageable
     * @return
     */
    @Query("{'author': ?0, 'status': 'ACTIVE'}")
    Page<Comment> findByAuthor(String author, Pageable pageable);

    // ======================= 통계용 쿼리 조회 =======================

    /**
     * 특정 게시글의 댓글 개수 (대댓글 포함)
     *
     * @param postId
     * @return
     */
    @Query(value = "{'postId': ?0, 'status': 'ACTIVE'}", count = true)
    long countByPostId(String postId);

    /**
     * 특정 게시글의 최상위 댓글 개수 (대댓글 제외)
     *
     * @param postId
     * @return
     */
    @Query(value = "{'postId': ?0, 'status': 'ACTIVE', 'parentCommentId': null}", count = true)
    long countRootCommentsByPostId(String postId);

    /**
     * 특정 게시글의 특정 댓글에 달린 대댓글 개수
     *
     * @param postId
     * @param parentCommentId
     * @return
     */
    @Query(value = "{'postId': ?0, 'parentCommentId': ?1, 'status': 'ACTIVE'}", count = true)
    long countRepliesByPostIdAndParentId(String postId, String parentCommentId);

    // ======================= 관리용 쿼리 조회 =======================

    /**
     * 특정 게시글의 모든 댓글 조회(삭제된 것 포함, 관리자용)
     *
     * @param postId
     * @return
     */
    @Query("{'postId': ?0}")
    List<Comment> findAllCommentsByPostIdIncludingDeleted(String postId);

    /**
     * 좋아요 수 기준 인기 댓글 조회
     *
     * @return
     */
    @Query("{'status': 'ACTIVE'}")
    List<Comment> findTop10ByOrderByLikeCountDesc();
}
