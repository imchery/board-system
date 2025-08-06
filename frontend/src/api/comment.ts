// 백엔드 API 기본 설정
import axios from "axios";
import {CommentRequest, CommentResponse, CommentUpdateRequest, PageResponse, ResponseVO} from "@/types/api.ts";

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
        return response.data // 백엔드 ResponseVO에서 data 부분만 추출
    },
    (error) => {
        console.error('API 에러:', error)
        return Promise.reject(error)
    }
)

// 댓글 관련 API 함수들
export const commentApi = {

    // 댓글 생성
    createComment: async (postId: string, request: CommentRequest): Promise<ResponseVO<CommentResponse>> => {
        return await apiClient.post(`/api/posts/${postId}/comments`, request)
    },

    // 댓글 수정
    updateComment: async (commentId: string, request: CommentUpdateRequest): Promise<ResponseVO<CommentResponse>> => {
        return await apiClient.put(`/api/comments/${commentId}`, request)
    },

    // 댓글 삭제
    deleteComment: async (commentId: string): Promise<ResponseVO<void>> => {
        return await apiClient.delete(`/api/comments/${commentId}`)
    },

    // 특정 게시글의 최상위 댓글 목록 조회(페이징)
    getRootComments: async (postId: string, page = 0, size = 10): Promise<ResponseVO<PageResponse<CommentResponse>>> => {
        const params = {page, size}
        return await apiClient.get(`/api/posts/${postId}/comments`, {params})
    },

    // 특정 댓글의 대댓글 미리보기 (처음 3개)
    getReplyPreview: async (postId: string, commentId: string): Promise<ResponseVO<CommentResponse[]>> => {
        return await apiClient.get(`/api/${postId}/comments/${commentId}/preview`)
    },

    // 특정 댓글의 대댓글 목록 조회(페이징)
    getReplies: async (postId: string, commentId: string, page = 0, size = 10): Promise<ResponseVO<PageResponse<CommentResponse>>> => {
        const params = {page, size}
        return await apiClient.get(`/api/posts/${postId}/comments/${commentId}/replies`, {params})
    },

    // 작성자별 댓글 조회 (마이페이지용)
    getCommentByAuthor: async (author: string, page = 0, size = 10): Promise<ResponseVO<PageResponse<CommentResponse>>> => {
        const params = {page, size}
        return await apiClient.get(`/api/comments/author/${author}`, {params})
    }
}



































