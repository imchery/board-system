package study.content.common.enums;

import org.springframework.data.domain.Sort;

/**
 * 댓글 정렬 타입 enum
 * <p>
 * 실무 포인트:
 * 1. enum을 사용하여 정렬 옵션의 타입 안전성 확보
 * 2. formString 메서드로 대소문자 무관하게 처리
 * 3. 기본값(LATEST) 설정으로 예외 상황 방지
 */
public enum CommentSortType {
    LATEST("최신순", "createdAt", "desc"),      // 최신 댓글이 위로
    OLDEST("오래된순", "createdAt", "asc");      // 오래된 댓글이 위로

    private final String description; // 한글 설명
    private final String fieldName; // MongoDB 필드명
    private final String direction; // 정렬 방향(asc/desc)

    CommentSortType(String description, String fieldName, String direction) {
        this.description = description;
        this.fieldName = fieldName;
        this.direction = direction;
    }

    public String getDescription() {
        return description;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getDirection() {
        return direction;
    }

    /**
     * 문자열을 CommentSortType 으로 변환
     * 대소문자 무관하게 처리하고, 잘못된 값이면 기본값 반환
     *
     * @param sortString 정렬 문자열(LATEST, OLDEST)
     * @return CommentSortType
     */
    public static CommentSortType fromString(String sortString) {
        if (sortString == null || sortString.trim()
                .isEmpty()) {
            return LATEST; // 기본값
        }
        try {
            return CommentSortType.valueOf(sortString);
        } catch (IllegalArgumentException e) {
            return LATEST;
        }
    }

    /**
     * MongoDB Sort 객체 생성 헬퍼 메서드
     * Spring Data MongoDB와 연동하기 위한 Sort 객체 생성
     *
     * @return
     */
    public Sort toMongoSort() {
        if ("desc".equals(this.direction)) {
            return Sort.by(Sort.Direction.DESC, fieldName);
        } else {
            return Sort.by(Sort.Direction.ASC, fieldName);
        }
    }
}
