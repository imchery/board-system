package study.content.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import study.common.lib.response.PageResponse;
import study.common.lib.response.ResponseVO;
import study.content.dto.post.PostRequest;
import study.content.dto.post.PostResponse;
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
    public ResponseVO createPost(
            @Valid @RequestBody PostRequest request,
            HttpServletRequest httpRequest
    ) {

        String username = (String) httpRequest.getAttribute("username");
        if (username == null) {
            // JWT 토큰이 없거나 유효하지 않은 경우
            return ResponseVO.authFail();
        }

        log.info("게시글 생성 요청: {} by user: {}", request.getTitle(), username);
        PostResponse post = postService.createPost(request, username);
        return ResponseVO.saveOk(post);
    }

    /**
     * 게시글 목록 조회(페이징)
     *
     * @param page
     * @param size
     * @return
     */
    @GetMapping
    public ResponseVO getPosts(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size) {
        log.info("게시글 목록 조회 - page: {}, size: {}", page, size);

        PageResponse<PostResponse> posts = postService.getPosts(page, size);
        return ResponseVO.ok(posts);
    }

    /**
     * 게시글 상세 조회 (조회수 증가)
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public ResponseVO getPost(@PathVariable String id) {
        log.info("게시글 상세 조회: {}", id);

        try {
            PostResponse post = postService.getPost(id);
            return ResponseVO.ok(post);
        } catch (Exception e) {
            return ResponseVO.noData();
        }
    }

    /**
     * 게시글 수정
     *
     * @param id
     * @param request
     * @return
     */
    @PutMapping("/{id}")
    public ResponseVO updatePost(@PathVariable String id,
                                 @Valid @RequestBody PostRequest request,
                                 HttpServletRequest httpRequest) {

        String username = (String) httpRequest.getAttribute("username");

        if (username == null) {
            return ResponseVO.authFail();
        }

        log.info("게시글 수정 요청: {} by user: {}", id, username);

        try {
            PostResponse post = postService.updatePost(id, request, username);
            return ResponseVO.updateOk(post);
        } catch (IllegalArgumentException e) {
            return ResponseVO.accessDenied();
        } catch (RuntimeException e) {
            return ResponseVO.error("게시글 수정에 실패했습니다.");
        }
    }

    /**
     * 게시글 삭제
     *
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public ResponseVO deletePost(@PathVariable String id, HttpServletRequest httpRequest) {

        String username = (String) httpRequest.getAttribute("username");

        if (username == null) {
            return ResponseVO.authFail();
        }
        log.info("게시글 삭제 요청: {} by user: {}", id, username);

        try {
            postService.deletePost(id, username);
            return ResponseVO.deleteOk();
        } catch (IllegalArgumentException e) {
            return ResponseVO.accessDenied();
        } catch (RuntimeException e) {
            return ResponseVO.error("게시글 삭제에 실패했습니다.");
        }
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
    public ResponseVO searchPosts(@RequestParam String keyword,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size) {
        log.info("게시글 검색: {}", keyword);

        PageResponse<PostResponse> posts = postService.searchPosts(keyword, page, size);
        return ResponseVO.ok(posts);
    }

    /**
     * 인기 게시글 조회 (조회수 기준 Top 10)
     *
     * @return
     */
    @GetMapping("/popular")
    public ResponseVO getPopularPosts() {
        log.info("인기 게시글 조회");

        List<PostResponse> posts = postService.getPopularPosts();
        return ResponseVO.ok(posts);
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
    public ResponseVO getPostsByAuthor(@PathVariable String author,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "10") int size) {
        log.info("작성자별 게시글 조회: {}", author);

        PageResponse<PostResponse> posts = postService.getPostsByAuthor(author, page, size);
        return ResponseVO.ok(posts);
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
    public ResponseVO getPostsByCategory(@PathVariable String category,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "10") int size) {
        log.info("카테고리별 게시글 조회: {}", category);

        PageResponse<PostResponse> posts = postService.getPostsByCategory(category, page, size);
        return ResponseVO.ok(posts);
    }

}
