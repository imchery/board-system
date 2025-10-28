// ======================= 백엔드 공통 응답 타입 =======================

/**
 * ResponseVO - 백엔드 공통 응답 타입
 * 사용 예시:
 * - ResponseVO<PostResponse> - 단일 게시글
 * - ResponseVO<PageResponse<PostResponse>> - 게시글 목록
 * - ResponseVO<void> - 데이터 없는 응답 (삭제 등)
 */
export interface ResponseVO<T = any> {
    /* 성공 여부 */
    result: boolean
    /* 응답메시지 */
    message: string
    /* 응답 데이터(제네릭) */
    data: T
    /* Validation 에러 목록 (있을 경우에만) */
    fieldErrors?: FieldErrorDetail[]
    /* 응답 생성 시간 */
    timestamp: number
}

/**
 * Validation 에러 상세 정보
 * @Valid 검증 실패 시 어떤 필드가 잘못됐는지 알려주는 타입
 */
export interface FieldErrorDetail {
    /* 에러가 발생한 필드명 */
    field: string
    /* 에러 메시지 */
    message: string
    /* 거부된 입력값 */
    rejectedValue: any
}

/**
 * 페이징 응답 타입
 */
export interface PageResponse<T> {
    /* 실제 데이터 배열 */
    content: T[]
    /* 현재 페이지 번호 (0부터 시작) */
    page: number
    /* 페이지 크기 */
    size: number
    /* 전체 요소 수 */
    totalElements: number
    /* 전체 페이지 수 */
    totalPages: number
    /* 첫 페이지 여부 */
    first: boolean
    /* 마지막 페이지 여부 */
    last: boolean
    /* 다음 페이지 존재 여부 */
    hasNext: boolean
    /* 이전 페이지 존재 여부 */
    hasPrevious: boolean
}

// ======================= 게시글 타입 =======================
/**
 * 게시글 응답 타입
 */
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
 * 게시글 생성/수정 요청 타입
 */
export interface PostRequest {
    title: string
    content: string
    category?: string
}

// ======================= 댓글 타입 =======================

/**
 * 댓글 응답 타입
 */
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

/**
 * 댓글 생성 요청 타입
 */
export interface CommentRequest {
    postId: string
    content: string
    parentCommentId?: string // 대댓글용 (null 이면 최상위 댓글)
}

/**
 * 댓글 수정 요청 타입
 */
export interface CommentUpdateRequest {
    content: string
}

// ======================= 에러 타입 =======================
export interface ApiError {
    response?: {
        status: number
        data?: ResponseVO<any>
    }
    message: string
}

// ======================= 인증 타입 =======================
export interface LoginRequest {
    username: string
    password: string
}

export interface LoginResponse {
    data?: {
        token: string
        username: string
    }
    message: string
}

// ======================= 좋아요 상태 관리 타입 =======================

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

// ======================= 회원가입 타입 =======================

/**
 * 회원가입 요청
 */
export interface SignupRequest {
    username: string
    password: string
    email: string
    nickname: string
}

/**
 * 회원가입 응답
 */
export interface SignupResponse {
    username: string
    email: string
    nickname: string
    message: string
}

/**
 * 중복 체크 응답
 */
export interface DuplicateRequest {
    available: boolean // true: 사용 가능, false: 중복
    message: string
}
