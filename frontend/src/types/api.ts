// ======================= 백엔드 공통 응답 타입 =======================
export interface ResponseVO<T = any> {
    result: boolean
    message: string
    data: T
    timestamp: number
}

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

// ======================= 게시글 타입 =======================
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
    isLikedByCurrentUser?: boolean
}

// ======================= 댓글 타입 =======================
export interface CommentResponse {
    id: string
    postId: string
    content: string
    author: string
    createdAt: string
    updatedAt: string
    parentCommentId?: string // null 가능
    isReply?: boolean        // 대댓글 여부

    likeCount?: number       // 좋아요 개수
    isLikedByCurrentUser?: boolean // 현재 사용자 좋아요 여부

    displayTime?: string     // 서버에서 제공하는 상태시간 ("3시간 전")
    canEdit?: boolean        // 수정 가능 여부
    canDelete?: boolean      // 삭제 가능 여부
    replyCount?: number      // 대댓글 개수
}

export interface CommentRequest {
    postId: string
    content: string
    parentCommentId?: string // 대댓글용 (null 이면 최상위 댓글)
}

export interface CommentUpdateRequest {
    content: string
}

// ======================= 에러 타입 =======================
export interface ApiError {
    response?: {
        status: number
        data?: any
    }
    message: string
}

// ======================= 인증 타입 =======================
export interface LoginRequest {
    username: string
    password: string
}

export interface LoginResponse {
    token: string | null
    username: string | null
    message: string
}

// ======================= 댓글 좋아요 상태 관리 타입 =======================

/**
 * 댓글 좋아요 토글 API 응답
 */
export interface CommentLikeResponse {
    id: string
    likeCount: number
    isLikedByCurrentUser: boolean
}

/**
 * 좋아요 토글 API 응답
 */
export interface LikeResponse {
    id: string
    likeCount: number
    isLikedByCurrentUser: boolean
}

// ======================= 사용자 타입 =======================

/**
 * 사용자 통계 응답
 */
export interface UserStatsResponse {
    username: string
    postCount: number
    commentCount: number
}
