package study.content.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.common.lib.response.PageResponse;
import study.common.lib.util.BasePagingUtil;
import study.content.common.enums.CommentSortType;
import study.content.dto.comment.CommentResponse;
import study.content.dto.post.PostResponse;
import study.content.dto.user.UserStatsResponse;
import study.content.repository.CommentRepository;
import study.content.repository.PostRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    /**
     * 사용자 기본 통계 조회
     *
     * @param username 사용자명
     * @return 사용자별 통계(게시글, 댓글 수)
     */
    public UserStatsResponse getUserStats(String username) {
        log.info("사용자 통계 조회: username: {}", username);

        // 1. 작성한 게시글 수
        long postCount = postRepository.countByAuthorAndStatus(username, "ACTIVE");

        // 2. 작성한 댓글 수
        long commentCount = commentRepository.countByAuthorAndStatus(username, "ACTIVE");

        UserStatsResponse stats = UserStatsResponse.builder()
                .username(username)
                .postCount(postCount)
                .commentCount(commentCount)
                .build();

        log.info("통계 조회 완료: username: {}, posts: {}, comments: {}", username, postCount, commentCount);

        return stats;
    }

    /**
     * 내가 작성한 게시글 목록 조회 (페이징)
     *
     * @param username
     * @param page
     * @param size
     * @return
     */
    public PageResponse<PostResponse> getMyPosts(String username, int page, int size) {
        log.info("내가 쓴 글 조회: username: {}, page: {}, size: {}", username, page, size);

        PageResponse<PostResponse> result = BasePagingUtil.createPageResponse(
                page, size,
                Sort.by("createdAt")
                        .descending(),
                pageable -> postRepository.findByAuthor(username, pageable),
                post -> {
                    PostResponse postResponse = PostResponse.from(post);

                    long commentCount = commentRepository.countByPostId(post.getId());
                    postResponse.setCommentCount(commentCount);
                    return postResponse;
                }
        );
        log.info("내가 쓴 글 조회 완료: username: {}, 총 {}개", username, result.getTotalElements());
        return result;
    }

    /**
     * 내가 작성한 댓글 목록 조회 (페이징)
     *
     * @param username
     * @param page
     * @param size
     * @return
     */
    public PageResponse<CommentResponse> getMyComments(String username, int page, int size) {
        log.info("내가 쓴 댓글 조회: username: {}, page: {}, size: {}", username, page, size);

        CommentSortType sortType = CommentSortType.LATEST;
        PageResponse<CommentResponse> result = BasePagingUtil.createPageResponse(
                page, size,
                sortType.toMongoSort(),
                pageable -> commentRepository.findByAuthor(username, pageable),
                CommentResponse::from
        );
        log.info("내가 쓴 댓글 조회 완료: username: {}, 총 {}개", username, result.getTotalElements());
        return result;
    }
}
