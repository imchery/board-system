package study.content.dto.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
    private String category;
    private String createdAt;
    private String updatedAt;

    // Entity -> DTO 변환 메서드
    public static PostResponse from(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .author(post.getAuthor())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .category(post.getCategory())
                .createdAt(post.getCreatedAt()
                        .toString())
                .updatedAt(post.getUpdatedAt()
                        .toString())
                .build();
    }
}
