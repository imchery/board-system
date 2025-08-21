package study.common.lib.util;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import study.common.lib.response.PageResponse;

import java.util.function.Function;

/**
 * 기본 페이징 유틸리티
 * - 파라미터 검증 및 안전 처리
 * - Pageable 생성 및 응답 변환
 * - 기본적인 정렬 패턴 제공
 */
public class BasePagingUtil {

    // 페이징 설정 상수
    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * 핵심 페이징 메서드
     *
     * @param page             페이지 번호(0부터 시작)
     * @param size             페이지 크기(1-100 사이)
     * @param sort             정렬 객체
     * @param repositoryMethod Repository 호출 함수
     * @param responseMapper   Entity -> Response 변환 함수
     * @param <T>
     * @param <R>
     * @return 페이징된 응답
     */
    public static <T, R> PageResponse<R> createPageResponse(
            int page, int size, Sort sort,
            Function<Pageable, Page<T>> repositoryMethod,
            Function<T, R> responseMapper
    ) {
        // 1. 파라미터 안전 처리
        int[] adjustedParams = adjustPagingParams(page, size);

        // 2. Pageable 생성 (정렬 포함)
        Pageable pageable = PageRequest.of(adjustedParams[0], adjustedParams[1], sort);

        // 3. Repository 호출 및 응답 변환
        Page<T> entityPages = repositoryMethod.apply(pageable);
        Page<R> responsePage = entityPages.map(responseMapper);

        return PageResponse.from(responsePage);
    }

    /**
     * 기본 정렬 페이징 (createAt DESC)
     *
     * @param page             페이지 번호
     * @param size             페이지 크기
     * @param repositoryMethod Repository 호출 함수
     * @param responseMapper   Entity -> Response 변환 함수
     * @param <T>
     * @param <R>
     * @return 최신순으로 정렬된 페이징 응답
     */
    public static <T, R> PageResponse<R> createDefaultPageResponse(
            int page, int size,
            Function<Pageable, Page<T>> repositoryMethod,
            Function<T, R> responseMapper
    ) {
        // 기본 정렬: 생성일시 내림차순 (최신순)
        Sort defaultSort = Sort.by(Sort.Direction.DESC, "createAt");
        return createPageResponse(page, size, defaultSort, repositoryMethod, responseMapper);
    }

    /**
     * 정렬 없는 페이징 (자연 순서)
     *
     * @param page             페이지 번호
     * @param size             페이지 크기
     * @param repositoryMethod Repository 호출 함수
     * @param responseMapper   Entity -> Response 변환 함수
     * @param <T>
     * @param <R>
     * @return 정렬되지 않은 페이징 응답
     */
    public static <T, R> PageResponse<R> createUnsortedPageResponse(
            int page, int size,
            Function<Pageable, Page<T>> repositoryMethod,
            Function<T, R> responseMapper
    ) {
        int[] adjustedParams = adjustPagingParams(page, size);
        Pageable pageable = PageRequest.of(adjustedParams[0], adjustedParams[1]);

        Page<T> entityPages = repositoryMethod.apply(pageable);
        Page<R> responsePage = entityPages.map(responseMapper);

        return PageResponse.from(responsePage);
    }

    // ============================= 헬퍼 메서드들 =============================

    /**
     * 페이징 파라미터 안전 처리
     * - 잘못된 값이 들어와도 안전하게 조정
     * - 운영 환경에서 API 오류 방지
     *
     * @param page 원본 페이지 번호
     * @param size 원본 페이지 크기
     * @return 조정된 파라미터 배열 [page, size]
     */
    public static int[] adjustPagingParams(int page, int size) {

        // 페이지 번호 조정 (음수면 0으로)
        if (page < 0) {
            page = 0;
        }

        // 페이지 크기 조정
        if (size < 1) {
            size = DEFAULT_PAGE_SIZE;
        } else if (size > MAX_PAGE_SIZE) {
            size = MAX_PAGE_SIZE;
        }
        return new int[]{page, size};
    }

    /**
     * 문자열 기반 정렬 객체 생성
     * - API 에서 정렬 파라미터를 문자열로 받을 때 사용
     *
     * @param sortField 정렬 필드명 (null 이면 "createAt")
     * @param direction 정렬 방향 ("asc" 또는 "desc")
     * @return Sort 객체
     */
    public static Sort createSortFromString(String sortField, String direction) {
        // 기본 필드명 설정
        if (sortField == null || sortField.trim()
                .isEmpty()) {
            sortField = "createAt";
        }

        // 정렬 방향 결정 (기본: desc)
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(sortDirection, sortField);
    }

    /**
     * 엄격한 파라미터 검증 (예외 발생)
     * - 안전한 조정 대신 예외를 발생시키는 검증
     *
     * @param page 페이지 번호
     * @param size 페이지 크기
     */
    public static void validatePagingParams(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("페이지 번호는 0 이상이어야 합니다: " + page);
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException(
                    "페이지 크기는 1-" + MAX_PAGE_SIZE + " 사이여야 합니다: " + size
            );
        }
    }
}
