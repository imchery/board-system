// 백엔드 API 기본 설정
import axios from "axios";
import {CommentResponse, PageResponse, PostResponse, ResponseVO, UserStatsResponse} from "@/types/api.ts";

const API_BASE_URL = 'http://localhost:9082'

const apiClient = axios.create({
    baseURL: API_BASE_URL,
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json'
    }
})

// 요청 인터셉터
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

// ===== 사용자 관련 API 함수들 =====
export const userApi = {
    /**
     * 내 통계 정보 조회 (마이페이지 대시보드용)
     */
    getMyStats: async (): Promise<ResponseVO<UserStatsResponse>> => {
        return await apiClient.get('/api/users/my-stats')
    },

    /**
     * 내가 작성한 게시글 목록 조회
     * @param page
     * @param size
     */
    getMyPosts: async (page = 0, size = 10): Promise<ResponseVO<PageResponse<PostResponse>>> => {
        const params = {page, size}
        return await apiClient.get('/api/users/my-posts', {params})
    },

    /**
     * 내가 작성한 댓글 목록 조회
     * @param page
     * @param size
     */
    getMyComments: async (page = 0, size = 10): Promise<ResponseVO<PageResponse<CommentResponse>>> => {
        const params = {page, size}
        return await apiClient.get('/api/users/my-comments', {params})
    }
}
