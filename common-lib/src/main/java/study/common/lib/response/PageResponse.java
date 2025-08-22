package study.common.lib.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 페이지 응답을 위한 공통 클래스
 * 모든 서비스에서 페이징 처리 시 사용
 *
 * @param <T>
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResponse<T> {

    /**
     * 페이지 내용 (실제 데이터)
     */
    private List<T> content;

    /**
     * 현재 페이지 번호(0부터 시작)
     */
    private int page;

    /**
     * 페이지 크기
     */
    private int size;

    /**
     * 전체 요소 개수
     */
    private long totalElements;

    /**
     * 전체 페이지 수
     */
    private int totalPages;

    /**
     * 첫 번째 페이지 여부
     */
    private boolean first;

    /**
     * 마지막 페이지 여부
     */
    private boolean last;

    /**
     * 다음 페이지 존재 여부
     */
    private boolean hasNext;

    /**
     * 이전 페이지 존재 여부
     */
    private boolean hasPrevious;

    /**
     * Spring Data Page -> PageResponse 변환
     *
     * @param page
     * @param <T>
     * @return
     */
    public static <T> PageResponse<T> from(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    /**
     * 빈 페이지 응답 생성
     * 검색 결과가 없거나 데이터가 없을 때 사용
     *
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param <T>
     * @return 빈 PageResponse 객체
     */
    public static <T> PageResponse<T> empty(int page, int size) {
        return PageResponse.<T>builder()
                .content(List.of())
                .page(page)
                .size(size)
                .totalElements(0L)
                .totalPages(0)
                .first(true)
                .last(true)
                .hasNext(false)
                .hasPrevious(false)
                .build();
    }

    /**
     * 기존 PageResponse의 메타데이터를 유지하면서 content만 교체
     *
     * @param originalPage 원본 PageResponse (메타데이터 소스)
     * @param newContent   새로운 컨텐츠 리스트
     * @param <T>          새로운 컨텐츠 타입
     * @param <R>          원본 컨텐츠 타입
     * @return 새로운 컨텐츠가 포함된 PageResponse
     */
    public static <T, R> PageResponse<T> withNewContent(PageResponse<R> originalPage, List<T> newContent) {
        return PageResponse.<T>builder()
                .content(newContent)
                .page(originalPage.getPage())
                .size(originalPage.getSize())
                .totalElements(originalPage.getTotalElements())
                .totalPages(originalPage.getTotalPages())
                .first(originalPage.isFirst())
                .last(originalPage.isLast())
                .hasNext(originalPage.isHasNext())
                .hasPrevious(originalPage.isHasPrevious())
                .build();
    }

}
