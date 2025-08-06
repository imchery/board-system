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
    createdAt: string
    updatedAt: string
    author: string
    viewCount?: number
    likeCount?: number
    commentCount?: number
    isLikedByCurrentUser?:boolean
}

export interface CommentResponse {
    id: string
    postId: string
    content: string
    author: string
    createdAt: string
    updatedAt: string
    parentCommentId?: string // null 가능
    isReply?: boolean // 대댓글 여부
}

export interface CommentRequest {
    postId: string
    content: string
    parentCommentId?: string // 대댓글용 (null이면 최상위 댓글)
}

export interface CommentUpdateRequest {
    content: string
}

export interface CommentStatsResponse {
    totalComments: number
    rootComments: number
    replies: number
}

// Axios 에러 타입
export interface ApiError {
    response?: {
        status: number
        data?: any
    }
    message: string
}

// 로그인 요청 타입
export interface LoginRequest{
    username: string
    password: string
}

// 로그인 응답 타입
export interface LoginResponse {
    token: string | null
    username: string | null
    message: string
}
