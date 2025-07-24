// 백엔드 ResponseVO 타입 정의
export interface ResponseVO<T = any> {
    result: boolean
    message: string
    data: T
    timestamp: number
}

// 페이징 응답 타입
export interface PageResponse<T> {
    content: T[]
    page: number
    size: number
    totalElements: number
    totalPages: number
    first: boolean
    last: boolean
    hasNext: boolean
    hasPrevious: boolean
}

// 게시글 타입
export interface PostResponse {
    id: string
    title: string
    content: string
    category: string
    createdDate: string
    updatedDate: string
    author: {
        id: string
        username: string
    }
}

// Axios 에러 타입
export interface ApiError {
    response?: {
        status: number
        data?: any
    }
    message: string
}
