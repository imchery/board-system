import {computed, ref} from "vue";
import {PageResponse} from "@/types/api.ts";

export interface PaginationState {
    currentPage: number
    pageSize: number
    totalElements: number
    totalPages: number
    loading: boolean
}

export const usePagination = (initialPageSize = 10) => {
    // 페이징 상태
    const currentPage = ref(1)
    const pageSize = ref(initialPageSize)
    const totalElements = ref(0)
    const totalPages = ref(0)
    const loading = ref(false)

    // 계산된 속성
    const hasData = computed(() => totalElements.value > 0)
    const hasPrevPage = computed(() => currentPage.value > 1)
    const hasNextPage = computed(() => currentPage.value < totalPages.value)

    /**
     * 페이지 응답 데이터로 상태 업데이트
     * @param response
     */
    const updateFromResponse = <T>(response: PageResponse<T>) => {
        totalElements.value = response.totalElements
        totalPages.value = response.totalPages
    }

    /**
     * 페이지 변경
     * @param page
     * @param callback
     */
    const changePage = (page: number, callback?: () => Promise<void>) => {
        currentPage.value = page
        if (callback) {
            callback()
        }
    }

    /**
     * 페이지 크기 변경
     * @param size
     * @param callback
     */
    const changePageSize = (size: number, callback?: () => Promise<void>) => {
        pageSize.value = size
        currentPage.value = 1
        if (callback) {
            callback()
        }
    }

    /**
     * 상태 리셋
     */
    const reset = () => {
        currentPage.value = 1
        totalElements.value = 0
        totalPages.value = 0
        loading.value = false
    }

    /**
     * 로딩 관리
     * @param asyncFn
     */
    const withLoading = async (asyncFn: () => Promise<void>) => {
        try {
            loading.value = true
            await asyncFn()
        } finally {
            loading.value = false
        }
    }

    return {
        // 반응형 상태
        currentPage,
        pageSize,
        totalElements,
        totalPages,
        loading,

        // 계산된 속성
        hasData,
        hasPrevPage,
        hasNextPage,

        // 메서드
        updateFromResponse,
        changePage,
        changePageSize,
        reset,
        withLoading,
    }

}
