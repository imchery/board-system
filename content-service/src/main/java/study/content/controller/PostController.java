package study.content.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import study.common.lib.response.ApiResponse;
import study.common.lib.response.PageResponse;
import study.content.dto.PostRequest;
import study.content.dto.PostResponse;
import study.content.service.PostService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * 게시글 생성
     *
     * @param request
     * @return
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PostResponse>> createPost(@Valid @RequestBody PostRequest request) {
        log.info("게시글 생성 요청: {}", request.getTitle());

        // TODO JWT에서 사용자 정보 추출(임시 하드코딩)
        String author = "testUser";
        PostResponse post = postService.createPost(request, author);
        ApiResponse<PostResponse> response = ApiResponse.success(post, "게시글이 성공적으로 생성되었습니다.");

        return ResponseEntity.ok(response);
    }

    /**
     * 게시글 목록 조회(페이징)
     *
     * @param page
     * @param size
     * @return
     */
    @GetMapping
    public ResponseEntity<PageResponse<PostResponse>> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("게시글 목록 조회 - page: {}, size: {}", page, size);
        PageResponse<PostResponse> posts = postService.getPosts(page, size);
        return ResponseEntity.ok(posts);
    }

    /**
     * 게시글 상세 조회 (조회수 증가)
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostResponse>> getPost(@PathVariable String id) {
        log.info("게시글 상세 조회: {}", id);

        PostResponse post = postService.getPost(id);
        ApiResponse<PostResponse> response = ApiResponse.success(post);
        return ResponseEntity.ok(response);
    }

    /**
     * 게시글 수정
     *
     * @param id
     * @param request
     * @return
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PostResponse>> updatePost(@PathVariable String id, @Valid @RequestBody PostRequest request) {
        log.info("게시글 수정 요청: {}", id);

        PostResponse post = postService.updatePost(id, request, "testUser");
        ApiResponse<PostResponse> response = ApiResponse.success(post, "게시글이 성공적으로 수정되었습니다.");
        return ResponseEntity.ok(response);
    }

    /**
     * 게시글 삭제
     *
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable String id) {
        log.info("게시글 삭제 요청: {}", id);

        postService.deletePost(id, "testUser");
        ApiResponse<Void> response = ApiResponse.success("게시글이 성공적으로 삭제되었습니다.");
        return ResponseEntity.ok(response);
    }

    /**
     * 게시글 검색
     *
     * @param keyword
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/search")
    public ResponseEntity<PageResponse<PostResponse>> searchPosts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("게시글 검색: {}", keyword);

        PageResponse<PostResponse> posts = postService.searchPosts(keyword, page, size);
        return ResponseEntity.ok(posts);
    }

    /**
     * 인기 게시글 조회 (조회수 기준 Top 10)
     *
     * @return
     */
    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<PostResponse>>> getPopularPosts() {
        log.info("인기 게시글 조회");

        List<PostResponse> posts = postService.getPopularPosts();
        ApiResponse<List<PostResponse>> response = ApiResponse.success(posts);
        return ResponseEntity.ok(response);
    }

    /**
     * 작성자별 게시글 조회
     *
     * @param author
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/author/{author}")
    public ResponseEntity<PageResponse<PostResponse>> getPostsByAuthor(
            @RequestParam String author,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("작성자별 게시글 조회: {}", author);
        PageResponse<PostResponse> posts = postService.getPostsByAuthor(author, page, size);
        return ResponseEntity.ok(posts);
    }

    /**
     * 카테고리별 게시글 조회
     *
     * @param category
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<PageResponse<PostResponse>> getPostsByCategory(
            @RequestParam String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("카테고리별 게시글 조회: {}", category);
        PageResponse<PostResponse> posts = postService.getPostsByCategory(category, page, size);
        return ResponseEntity.ok(posts);
    }

}
