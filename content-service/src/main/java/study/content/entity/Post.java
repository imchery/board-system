package study.content.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "posts")
public class Post {

    @Id
    private String id;

    private String title;

    private String content;

    private String author; // JWT 토큰에서 추출한 사용자명

    private Integer viewCount;

    private Integer likeCount;

    private List<String> likedUsers; // 좋아요한 사람들 목록

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // 게시글 상태(ACTIVE, DELETED)
    private PostStatus status;

    // 카테고리 TODO 나중에 추가
    private String category;

    public enum PostStatus {
        ACTIVE, DELETED;
    }

    // 빌더 패턴용 기본값 설정
    public static PostBuilder builder() {
        return new PostBuilder()
                .viewCount(0)
                .likeCount(0)
                .likedUsers(new ArrayList<>()) // 빈 리스트로 초기화
                .status(PostStatus.ACTIVE);
    }

    // ============================== 비즈니스 메서드 ================================

    /**
     * 게시글 수정
     *
     * @param title
     * @param content
     * @param category
     */
    public void updatePost(String title, String content, String category) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 조회수 증가
     */
    public void increaseViewCount() {
        this.viewCount++;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 좋아요 추가
     *
     * @param username
     */
    public void addLike(String username) {
        if (!this.likedUsers.contains(username)) {
            this.likedUsers.add(username);
            this.likeCount++;
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * 좋아요 제거
     *
     * @param username
     */
    public void removeLike(String username) {
        if (this.likedUsers.remove(username)) { // remove는 성공 시 true 반환
            this.likeCount--;
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * 좋아요 여부 확인
     *
     * @param username
     * @return
     */
    public boolean isLikeBy(String username) {
        return this.likedUsers.contains(username);
    }

    /**
     * 게시글 삭제(Soft Delete)
     */
    public void delete() {
        this.status = PostStatus.DELETED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 작성자 권한 확인
     *
     * @param username
     * @return
     */
    public boolean isAuthor(String username) {
        return this.author != null && this.author.equals(username);
    }

    /**
     * 활성화 상태 확인
     *
     * @return
     */
    public boolean isActive() {
        return this.status == PostStatus.ACTIVE;
    }

    /**
     * 삭제 상태 확인
     *
     * @return
     */
    public boolean isDeleted() {
        return this.status == PostStatus.DELETED;
    }

}
