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

/**
 * 페이징 요청 파라미터
 */
export interface PagingParams {
    page?: number
    size?: number
    sort?: CommentSortDirection
}

/**
 * 검색 파라미터
 */
export interface SearchParams extends PagingParams {
    keyword?: string
    category?: string
    author?: string
    startDate?: string
    endDate?: string
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

export interface CommentStatsResponse {
    totalComments: number
    rootComments: number
    replies: number
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

// ======================= 정렬 관련 타입 =======================

/**
 * 댓글 정렬 방향
 */
export type CommentSortDirection = 'latest' | 'oldest'

/**
 * 정렬 옵션 인터페이스 (UI 컴포넌트에서 사용)
 */
export interface SortOption {
    value: CommentSortDirection
    label: string
    description?: string // 추가 설명
}

// ======================= API 응답 헬퍼 타입 =======================

/**
 * API 호출 결과를 처리하기 위한 유니온 타입
 * 유니온 타입: 이것 또는 저것 타입
 */
export type ApiResult<T> = {
    success: true
    data: T
} | {
    success: false
    error: string
}

/**
 * 로딩 상태를 포함한 데이터 타입
 */
export interface DataWithLoading<T> {
    data: T | null
    loading: boolean
    error: string | null
}

// ======================= 타입 가드 함수들 =======================

/**
 * CommentResponse 타입 가드
 * @param obj
 */
export const isCommentResponse = (obj: any): obj is CommentResponse => {
    return obj &&
        typeof obj.id === 'string' &&
        typeof obj.postId === 'string' &&
        typeof obj.content === 'string' &&
        typeof obj.author === 'string' &&
        typeof obj.createdAt === 'string'
}

/**
 * PageResponse 타입 가드
 * @param obj
 */
export const isPageResponse = <T>(obj: any): obj is PageResponse<T> => {
    return obj &&
        Array.isArray(obj.constent) &&
        typeof obj.page === 'number' &&
        typeof obj.size === 'number' &&
        typeof obj.totalElements === 'number' &&
        typeof obj.totalPages === 'number'
}

/**
 * 정렬 방향 검증
 * @param sort
 */
export const isValidSortDirection = (sort: string): sort is CommentSortDirection => {
    return sort === 'latest' || sort === 'oldest'
}

// ======================= 댓글 좋아요 상태 관리 타입 =======================

/**
 * 댓글 좋아요 토글 함수 시그니처
 */
export type ToggleCommentLikeFunction = (commentId: string) => Promise<ResponseVO<CommentLikeResponse>>

/**
 * 댓글 좋아요 토글 API 응답
 */
export interface CommentLikeResponse {
    id: string
    likeCount: number
    isLikedByCurrentUser: boolean
}

/**
 * 댓글 좋아요 UI 상태
 */
export interface CommentLikeState {
    loading: boolean        // 좋아요 토글 중인지
    likeCount: number       // 현재 좋아요 개수
    isLiked: boolean        // 현재 좋아요 상태
}

/**
 * 댓글 좋아요 액션 결과
 */
export interface CommentLikeActionResult {
    success: boolean        // 성공여부
    newLikeCount?: number   // 변경된 좋아요 개수
    newIsLiked?: boolean    // 변경된 좋아요 상태
    message?: string        // 에러 메시지 또는 성공 메시지
}
