package study.content.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import study.common.lib.exception.BaseException;
import study.common.lib.exception.ErrorCode;
import study.common.lib.response.PageResponse;
import study.common.lib.response.ResponseVO;
import study.content.dto.post.PostRequest;
import study.content.dto.post.PostResponse;
import study.content.entity.Post;
import study.content.service.PostService;

import java.util.List;

/**
 * 게시글 관련 API Controller
 * 게시글 CRUD, 검색, 인기 게시글 조회 등의 기능 제공
 */
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
    public ResponseVO<PostResponse> createPost(
            @Valid @RequestBody PostRequest request,
            HttpServletRequest httpRequest
    ) {

        String username = extractUsername(httpRequest);

        log.info("게시글 생성 요청: {} by user: {}", request.getTitle(), username);
        PostResponse post = postService.createPost(request, username);
        return ResponseVO.saveOk(post);
    }

    /**
     * 게시글 목록 조회(페이징)
     * 활성 상태의 게시글만 조회되며, 최신순으로 정렬
     *
     * @param page 페이지 번호(0부터 시작)
     * @param size 페이지 크기
     * @return 게시글 목록 (페이지 정보 포함)
     */
    @GetMapping
    public ResponseVO<PageResponse<PostResponse>> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("게시글 목록 조회 - page: {}, size: {}", page, size);

        PageResponse<PostResponse> posts = postService.getPosts(page, size);
        return ResponseVO.ok(posts);
    }

    /**
     * 게시글 상세 조회
     * 조회 시 조회수가 자동으로 1 증가
     * 비로그인 사용자도 조회 가능
     *
     * @param id          게시글 ID
     * @param httpRequest HTTP 요청(username이 있으면 로그인 사용자)
     * @return 게시글 상세정보
     */
    @GetMapping("/{id}")
    public ResponseVO<PostResponse> getPost(@PathVariable String id, HttpServletRequest httpRequest) {
        String username = extractUsername(httpRequest);

        log.info("게시글 상세 조회 - postId: {}, viewer: {}",
                id, username);

        PostResponse post = postService.getPost(id, username);
        return ResponseVO.ok(post);
    }

    /**
     * 게시글 검색
     * 제목과 내용에서 키워드 검색
     *
     * @param keyword 검색 키워드
     * @param page    페이지 번호
     * @param size    페이지 크기
     * @return 검색된 게시글 목록
     */
    @GetMapping("/search")
    public ResponseVO<PageResponse<PostResponse>> searchPosts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("게시글 검색 - keyword: {}, page: {}, size: {}",
                keyword, page, size);

        PageResponse<PostResponse> posts = postService.searchPosts(keyword, page, size);
        return ResponseVO.ok(posts);
    }

    /**
     * 인기 게시글 조회
     * 조회수 기준 상위 10개 게시글을 반환
     *
     * @return 인기 게시글 목록(최대 10개)
     */
    @GetMapping("/popular")
    public ResponseVO<List<PostResponse>> getPopularPosts() {
        log.info("인기 게시글 조회");

        List<PostResponse> posts = postService.getPopularPosts();
        return ResponseVO.ok(posts);
    }

    /**
     * 게시글 수정
     * 작성자만 수정 가능
     *
     * @param id          게시글 ID
     * @param request     수정 요청(제목, 내용 등)
     * @param httpRequest HTTP 요청(username 포함)
     * @return 수정된 게시글 정보
     */
    @PutMapping("/{id}")
    public ResponseVO<PostResponse> updatePost(
            @PathVariable String id,
            @Valid @RequestBody PostRequest request,
            HttpServletRequest httpRequest) {

        String username = extractUsername(httpRequest);

        log.info("게시글 수정 요청 - postId: {}, author: {}", id, username);

        PostResponse post = postService.updatePost(id, request, username);
        return ResponseVO.updateOk(post);
    }

    /**
     * 게시글 삭제(소프트 삭제)
     * 작성자만 삭제 가능
     * 실제로는 상태만 DELETED로 변경되며 데이터는 유지됨
     *
     * @param id          게시글 ID
     * @param httpRequest HTTP 요청(username 포함)
     * @return 삭제 완료 응답
     */
    @DeleteMapping("/{id}")
    public ResponseVO<Void> deletePost(@PathVariable String id, HttpServletRequest httpRequest) {

        String username = extractUsername(httpRequest);

        log.info("게시글 삭제 요청 - postId: {}, author: {}", id, username);

        postService.deletePost(id, username);
        return ResponseVO.deleteOk();
    }

    /**
     * HTTP 요청에서 username 추출 및 검증
     * username이 없으면 UnauthorizedException 발생
     *
     * @param request HTTP 요청
     * @return username
     */
    private String extractUsername(HttpServletRequest request) {
        String username = (String) request.getAttribute("username");

//        if (username == null) {
//            log.warn("인증되지 않은 요청");
//            throw new BaseException(ErrorCode.UNAUTHORIZED);
//        }
        return username;
    }
}
