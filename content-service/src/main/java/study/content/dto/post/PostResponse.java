package study.content.dto.post;

import lombok.*;
import study.content.entity.Post;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponse {

    private String id;
    private String title;
    private String content;
    private String author;
    private Integer viewCount;
    private Integer likeCount;
    private Boolean isLikedByCurrentUser; // 현재 사용자가 좋아요했는지
    private String category;
    private String createdAt;
    private String updatedAt;
    private Long commentCount;

    // Entity -> DTO 변환 메서드
    public static PostResponse from(Post post, String currentUser) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .author(post.getAuthor())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .isLikedByCurrentUser(currentUser != null ? post.isLikeBy(currentUser) : null)
                .category(post.getCategory())
                .createdAt(post.getCreatedAt()
                        .toString())
                .updatedAt(post.getUpdatedAt()
                        .toString())
                .build();
    }

    // 기존 호환성을 위한 오버로딩
    public static PostResponse from(Post post) {
        return from(post, null);
    }

}
