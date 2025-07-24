import axios from "axios";
import {PageResponse, PostResponse, ResponseVO} from "@/types/api.ts";

// 백엔드 API 기본 설정
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

// 게시글 관련 API 함수들
export const postApi = {
    // 게시글 목록 조회 (페이징)
    getPosts: async (page = 0, size = 10, keyword = ''): Promise<ResponseVO<PageResponse<PostResponse>>> => {
        const params: any = {page, size}
        if (keyword) {
            return await apiClient.get('/api/posts/search', {params: {...params, keyword}})
        }
        return await apiClient.get('/api/posts', {params})
    },

    // 게시글 상세 조회
    getPost: async (id: string): Promise<ResponseVO<PostResponse>> => {
        return await apiClient.get(`/api/posts/${id}`)
    },

    // 게시글 생성
    createPost: async (postData: {
        title: string;
        content: string;
        category?: string
    }): Promise<ResponseVO<PostResponse>> => {
        return await apiClient.post('/api/posts', postData)
    },

    // 게시글 수정
    updatePost: async (id: string, postData: {
        title: string;
        content: string;
        category?: string
    }): Promise<ResponseVO<PostResponse>> => {
        return await apiClient.put(`/api/posts/${id}`, postData)
    },

    // 게시글 삭제
    deletePost: async (id: string): Promise<ResponseVO<void>> => {
        return await apiClient.delete(`/api/posts/${id}`)
    },

    // 게시글 좋아요 토글
    toggleLike: async (id: string): Promise<ResponseVO<PostResponse>> => {
        return await apiClient.put(`/api/posts/${id}/toggle-like`)
    },

    // 인기 게시글 조회
    getPopularPosts: async (): Promise<ResponseVO<PostResponse[]>> => {
        return await apiClient.get('/api/posts/popular')
    },
}
