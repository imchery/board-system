// 백엔드 API 기본 설정
import axios from "axios";
import {
    CommentLikeResponse,
    CommentRequest,
    CommentResponse,
    CommentUpdateRequest,
    PageResponse,
    ResponseVO
} from "@/types/api.ts";

const API_BASE_URL = 'http://localhost:9082'

// Axios 인스턴스 생성
const apiClient = axios.create({
    baseURL: API_BASE_URL,
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json'
    }
})

// 요청 인터셉터 (JWT 토큰 자동 추가)
apiClient.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token')
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    (error) => {
        return Promise.reject(error)
    }
)

// 응답 인터셉터 (에러 처리)
apiClient.interceptors.response.use(
    (response) => {
        return response.data // 백엔드 ResponseVO 에서 data 부분만 추출
    },
    (error) => {
        console.error('API 에러:', error)
        return Promise.reject(error)
    }
)

/**s
 * 댓글 정렬 타입
 */
export type CommentSortType = 'LATEST' | 'OLDEST'

/**
 * 댓글 관련 API 함수들
 * - 기본 값은 'LATEST' (최신순)
 */
export const commentApi = {

    // ======================= 생성/수정/삭제 =======================

    /**
     * 댓글 생성
     * @param postId
     * @param request
     */
    createComment: async (
        postId: string,
        request: CommentRequest
    ): Promise<ResponseVO<CommentResponse>> => {
        return await apiClient.post(`/api/posts/${postId}/comments`, request)
    },

    /**
     * 댓글 수정
     * @param commentId
     * @param request
     */
    updateComment: async (
        commentId: string,
        request: CommentUpdateRequest
    ): Promise<ResponseVO<CommentResponse>> => {
        return await apiClient.put(`/api/comments/${commentId}`, request)
    },

    /**
     * 댓글 삭제
     * @param commentId
     */
    deleteComment: async (commentId: string): Promise<ResponseVO<void>> => {
        return await apiClient.delete(`/api/comments/${commentId}`)
    },

    // ======================= 조회 (정렬 지원) =======================

    /**
     * 특정 게시글의 최상위 댓글 목록 조회 (정렬 지원)
     * @param postId 게시글 ID
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기 (기본 10개)
     * @param sort 정렬 방식 (LATEST: 최신순, OLDEST: 오래된순)
     */
    getRootComments: async (
        postId: string,
        page = 0,
        size = 10,
        sort: CommentSortType = 'LATEST'
    ): Promise<ResponseVO<PageResponse<CommentResponse>>> => {
        const params = {page, size, sort}
        return await apiClient.get(`/api/posts/${postId}/comments`, {params})
    },

    /**
     * 특정 댓글의 대댓글 미리보기 (처음 3개)
     * 정렬 없음 - 최신순
     * @param postId
     * @param commentId
     */
    getReplyPreview: async (
        postId: string,
        commentId: string
    ): Promise<ResponseVO<CommentResponse[]>> => {
        return await apiClient.get(`/api/posts/${postId}/comments/${commentId}/preview`)
    },

    /**
     * 특정 댓글의 대댓글 목록 조회 (정렬 지원)
     * @param postId 게시글 ID
     * @param commentId 부모 댓글 ID
     * @param page 페이지 번호
     * @param size 페이지 크기
     */
    getReplies: async (
        postId: string,
        commentId: string,
        page = 0,
        size = 10
    ): Promise<ResponseVO<PageResponse<CommentResponse>>> => {
        const params = {page, size}
        return await apiClient.get(`/api/posts/${postId}/comments/${commentId}/replies`, {params})
    },

    /**
     * 작성자별 댓글 조회 (마이페이지용)
     * @param author
     * @param page
     * @param size
     * @param sort
     */
    getCommentByAuthor: async (
        author: string,
        page = 0,
        size = 10,
        sort: CommentSortType = 'LATEST'
    ): Promise<ResponseVO<PageResponse<CommentResponse>>> => {
        const params = {page, size, sort}
        return await apiClient.get(`/api/comments/author/${author}`, {params})
    },

    // ======================= 기타 조회 =======================

    /**
     * 관리자용 댓글 조회 (삭제된 것 포함, 정렬 X)
     * @param postId
     */
    getAllCommentsForAdmin: async (postId: string): Promise<ResponseVO<CommentResponse[]>> => {
        return await apiClient.get(`/api/admin/posts/{postId}/comments`)
    },

    /**
     * 댓글 통계 조회
     * @param postId
     */
    getCommentStats: async (postId: string): Promise<ResponseVO<number>> => {
        return await apiClient.get(`/api/posts/{postId}/comments/stats`)
    },

    /**
     * 대댓글 개수 조회
     * @param postId
     * @param commentId
     */
    getReplyCount: async (
        postId: string,
        commentId: string
    ): Promise<ResponseVO<number>> => {
        return await apiClient.get(`/api/posts/{postId}/comments/{commentId}/count`)
    },

    /**
     * 댓글 좋아요 토글
     * @param commentId 댓글 ID
     */
    toggleLike: async (commentId: string): Promise<ResponseVO<CommentLikeResponse>> => {
        try {
            console.log('댓글 좋아요 토글 요청:', commentId)
            return await apiClient.post(`/api/comments/${commentId}/like`)

        } catch (error) {
            console.error('댓글 좋아요 토글 실패:', error)
            throw error
        }
    }
}
