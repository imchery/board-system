package study.content.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import study.content.entity.Post;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends MongoRepository<Post, String> {

    // 활성 상태인 게시글만 조회(삭제된 글 제외)
    @Query("{status:  'ACTIVE'}")
    Page<Post> findAllActivePosts(Pageable pageable);

    // 제목이나 내용으로 검색(활성 상태만)
    @Query("{'status': 'ACTIVE', '$or': [{'title': {$regex: ?0, $options: 'i'}}, {'content': {$regex: ?0, $options: 'i'}}]}")
    Page<Post> findByTitleOrContentContaining(String keyword, Pageable pageable);

    // 작성자별 게시글 조회(활성 상태만)
    @Query("{'author': ?0, 'status':  'ACTIVE'}")
    Page<Post> findByAuthor(String author, Pageable pageable);

    // 카테고리별 게시글 조회(활성 상태만)
    @Query("{'category': ?0, 'status':  'ACTIVE'}")
    Page<Post> findByCategory(String category, Pageable pageable);

    // ID로 활성 게시글 조회
    @Query("{'_id': ?0, 'status':  'ACTIVE'}")
    Optional<Post> findActivePostById(String id);

    // 조회수 상위 게시글
    @Query("{'status': 'ACTIVE'}")
    List<Post> findTop10ByOrderByViewCountDesc();
}
