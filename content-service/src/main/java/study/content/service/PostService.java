package study.content.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.common.lib.response.PageResponse;
import study.content.dto.post.PostRequest;
import study.content.dto.post.PostResponse;
import study.content.entity.Comment;
import study.content.entity.Like;
import study.content.entity.Post;
import study.content.repository.*;

import java.util.List;
import java.util.function.Function;

/**
 * 게시글 비즈니스 로직 처리 Service
 * 게시글 CRUD, 검색, 인기 게시글 조회 등의 비즈니스 로직 담당
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;

    /**
     * 게시글 생성
     *
     * @param request 게시글 생성 요청
     * @param author  작성자
     * @return 생성된 게시글 정보
     */
    @Transactional
    public PostResponse createPost(PostRequest request, String author) {
        log.info("게시글 생성 - title: {}, author: {}", request.getTitle(), author);

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .author(author)
                .category(request.getCategory())
                .build();

        Post savedPost = postRepository.save(post);
        log.info("게시글 생성 완료 - postId: {}", savedPost.getId());

        return PostResponse.from(savedPost);
    }

    /**
     * 게시글 목록 조회(페이징)
     * 활성 상태의 게시글만 조회, 최신순으로 정렬
     *
     * @param page 페이지 번호(0부터 시작)
     * @param size 페이지 크기
     * @return 게시글 목록
     */
    public PageResponse<PostResponse> getPosts(int page, int size) {
        log.debug("게시글 목록 조회 - page: {}, size: {}", page, size);

        return getPostsWithPaging(page, size, postRepository::findAllActivePosts);
    }

    /**
     * 게시글 상세 조회(조회수 증가)
     *
     * @param id          게시글 ID
     * @param currentUser 현재 사용자 (비로그인 시 null)
     * @return 게시글 상세 정보
     */
    @Transactional
    public PostResponse getPost(String id, String currentUser) {
        log.debug("게시글 상세 조회 - postId: {}, viewer: {}",
                id, currentUser != null ? currentUser : "비로그인");

        Post post = findActivePostById(id);

        // 조회수 증가
        post.increaseViewCount();
        postRepository.save(post);

        log.debug("조회수 증가 - postId: {}, viewCount: {}", id, post.getViewCount());

        PostResponse response = PostResponse.from(post, currentUser);

        // 댓글 수 조회
        long commentCount = commentRepository.countByPostId(id);
        response.setCommentCount(commentCount);

        return response;
    }

    /**
     * 게시글 검색
     * 제목과 내용에서 키워드를 검색
     *
     * @param keyword 검색 키워드
     * @param page    페이지 번호
     * @param size    페이지 크기
     * @return 검색된 게시글 목록
     */
    public PageResponse<PostResponse> searchPosts(String keyword, int page, int size) {
        log.info("게시글 검색 - keyword: {}, page: {}, size: {}", keyword, page, size);

        return getPostsWithPaging(page, size, pageable -> postRepository.findByTitleOrContentContaining(keyword, pageable));
    }

    /**
     * 인기 게시글 조회(조회수 기준 Top10)
     *
     * @return 인기 게시글 목록 최대 10개
     */
    public List<PostResponse> getPopularPosts() {
        log.debug("인기 게시글 조회");

        List<Post> popularPosts = postRepository.findTop10ByOrderByViewCountDesc();

        return popularPosts.stream()
                .map(PostResponse::from)
                .toList();
    }

    /**
     * 게시글 수정
     * 작성자만 수정가능
     *
     * @param id      게시글 ID
     * @param request 수정 요청
     * @param author  요청자
     * @return 수정된 게시글 정보
     */
    @Transactional
    public PostResponse updatePost(String id, PostRequest request, String author) {
        log.info("게시글 수정 - postId: {}, author: {}", id, author);

        Post post = findActivePostById(id);
        validateAuthor(post, author);

        post.updatePost(request.getTitle(), request.getContent(), request.getCategory());

        Post updatedPost = postRepository.save(post);
        log.info("게시글 수정 완료 - postId: {}", updatedPost.getId());

        return PostResponse.from(updatedPost);
    }

    /**
     * 게시글 삭제 (Soft Delete)
     * - 순서
     * 댓글 좋아요 delete -> 댓글 전체 soft delete -> 게시글 좋아요 delete -> 게시글 soft delete
     *
     * @param id
     * @param author
     */
    @Transactional
    public void deletePost(String id, String author) {
        log.info("게시글 삭제 시작 - postId: {}, author: {}", id, author);

        // 1. 게시글 존재 및 권한 확인
        Post post = findActivePostById(id);
        validateAuthor(post, author);

        // 2. 모든 댓글 조회 (부모댓글 + 대댓글)
        List<Comment> allComments = commentRepository.findAllActiveCommentsByPostId(id);
        log.debug("삭제 대상 댓글 수: {}", allComments.size());

        // 3. 각 댓글의 좋아요 물리 삭제
        long totalCommentLikesDeleted = 0;
        for (Comment comment : allComments) {

            // 3-1. 댓글 좋아요 물리 삭제
            long deleted = likeRepository.deleteByTargetIdAndTargetType(comment.getId(), Like.TargetType.COMMENT);
            totalCommentLikesDeleted += deleted;

            // 3-2. 댓글 소프트 삭제
            comment.delete();
        }

        // 3-3. 댓글 일괄 저장
        if (!allComments.isEmpty()) {
            commentRepository.saveAll(allComments);
        }

        log.debug("댓글 및 댓글 좋아요 처리 완료 - 댓글: {}개, 좋아요: {}개",
                allComments.size(), totalCommentLikesDeleted);

        // 4. 게시글 좋아요 물리 삭제
        long postLikesDeleted = likeRepository.deleteByTargetIdAndTargetType(id, Like.TargetType.POST);

        log.debug("게시글 좋아요 삭제 완료: {}개", postLikesDeleted);

        // 5. 게시글 soft delete
        post.delete();
        postRepository.save(post);

        log.info("게시글 삭제 완료 - postId: {}, 댓글: {}개, 댓글좋아요: {}개, 게시글좋아요: {}개",
                id, allComments.size(), totalCommentLikesDeleted, postLikesDeleted);
    }

    // ==================================================== 프라이빗 헬퍼 메서드 ====================================================

    /**
     * 페이징 처리 공통 로직(함수형 인터페이스 활용)
     *
     * @param page
     * @param size
     * @param repositoryMethod
     * @return
     */
    private PageResponse<PostResponse> getPostsWithPaging(int page, int size,
                                                          Function<Pageable, Page<Post>> repositoryMethod) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt")
                .descending());
        Page<Post> postPage = repositoryMethod.apply(pageable);
        Page<PostResponse> responsePage = postPage.map(PostResponse::from);

        return PageResponse.from(responsePage);
    }

    /**
     * 활성 게시글 조회(공통 로직)
     *
     * @param id
     * @return
     */
    private Post findActivePostById(String id) {
        return postRepository.findActivePostById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다: " + id));
    }


    /**
     * 작성자 권한 검증(공통 로직)
     *
     * @param post
     * @param author
     */
    private void validateAuthor(Post post, String author) {
        if (!post.isAuthor(author)) {
            throw new IllegalArgumentException("게시글에 대한 권한이 없습니다");
        }
    }

}
