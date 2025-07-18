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
import study.content.entity.Post;
import study.content.repository.PostRepository;

import java.util.List;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;

    /**
     * 게시글 생성
     *
     * @param request
     * @param author
     * @return
     */
    @Transactional
    public PostResponse createPost(PostRequest request, String author) {
        log.info("Creating post with title: {} by author: {}", request.getTitle(), author);

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .author(author)
                .category(request.getCategory())
                .build();

        Post savedPost = postRepository.save(post);
        log.info("Post created with id: {}", savedPost.getId());

        return PostResponse.from(savedPost);
    }

    /**
     * 게시글 목록 조회(페이징)
     *
     * @param page
     * @param size
     * @return
     */
    public PageResponse<PostResponse> getPosts(int page, int size) {
        log.info("Fetching posts - page: {}, size: {}", page, size);

        return getPostsWithPaging(page, size, postRepository::findAllActivePosts);
    }

    /**
     * 게시글 상세 조회(조회수 증가)
     *
     * @param id
     * @return
     */
    @Transactional
    public PostResponse getPost(String id) {
        log.info("Fetching post with id: {}", id);

        Post post = findActivePostById(id);

        // 조회수 증가
        post.increaseViewCount();
        postRepository.save(post);

        log.info("Post viewed - id: {}, new view count: {}", id, post.getViewCount());

        return PostResponse.from(post);
    }

    /**
     * 게시글 수정
     *
     * @param id
     * @param request
     * @param author
     * @return
     */
    @Transactional
    public PostResponse updatePost(String id, PostRequest request, String author) {
        log.info("Updating post with id: {} by author: {}", id, author);

        Post post = findActivePostById(id);
        validateAuthor(post, author);

        post.updatePost(request.getTitle(), request.getContent(), request.getCategory());

        Post updatePost = postRepository.save(post);
        log.info("Post updated - id: {}", updatePost.getId());

        return PostResponse.from(updatePost);
    }

    /**
     * 게시글 삭제 (Soft Delete)
     *
     * @param id
     * @param author
     */
    @Transactional
    public void deletePost(String id, String author) {
        log.info("Deleting post id: {} by author: {}", id, author);

        Post post = findActivePostById(id);
        validateAuthor(post, author);

        post.delete();
        postRepository.save(post);
        log.info("Post soft deleted - id: {}", id);
    }

    /**
     * 게시글 검색
     *
     * @param keyword
     * @param page
     * @param size
     * @return
     */
    public PageResponse<PostResponse> searchPosts(String keyword, int page, int size) {
        log.info("Searching posts with keyword: {}", page);

        return getPostsWithPaging(page, size, pageable -> postRepository.findByTitleOrContentContaining(keyword, pageable));
    }

    /**
     * 인기 게시글 조회(조회수 기준)
     *
     * @return
     */
    public List<PostResponse> getPopularPosts() {
        log.info("Fetching popular posts");

        List<Post> popularPosts = postRepository.findTop10ByOrderByViewCountDesc();

        return popularPosts.stream()
                .map(PostResponse::from)
                .toList();
    }

    /**
     * 작성자별 게시글 조회
     *
     * @param author
     * @param page
     * @param size
     * @return
     */
    public PageResponse<PostResponse> getPostsByAuthor(String author, int page, int size) {
        log.info("Fetching posts by author: {}", author);

        return getPostsWithPaging(page, size, pageable -> postRepository.findByAuthor(author, pageable));
    }

    /**
     * 카테고리별 게시글 조회
     *
     * @param category
     * @param page
     * @param size
     * @return
     */
    public PageResponse<PostResponse> getPostsByCategory(String category, int page, int size) {
        log.info("Fetching posts by category: {}", category);

        return getPostsWithPaging(page, size, pageable -> postRepository.findByCategory(category, pageable));
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
