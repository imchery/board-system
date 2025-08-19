import {ElMessage} from 'element-plus'
import {useRouter} from 'vue-router'
import type {ApiError} from '@/types/api'

/**
 * API 에러 공통 처리 함수
 * @param error - API 에러 객체
 * @param options - 에러 처리 옵션
 */
export const handleApiError = (
    error: any,
    options: {
        /** 404 에러 시 리다이렉트할 경로 (기본: '/posts') */
        notFoundRedirect?: string
        /** 401 에러 시 리다이렉트할 경로 (기본: '/login') */
        unauthorizedRedirect?: string
        /** 커스텀 에러 메시지 매핑 */
        customErrorMessages?: {
            [statusCode: number]: string
        }
        /** 특정 상태 코드에서 리다이렉트 안 함 */
        skipRedirect?: boolean
    } = {}
) => {
    const router = useRouter()
    console.error('API 에러:', error)

    const apiError = error as ApiError
    const statusCode = apiError.response?.status

    // 커스텀 메시지가 있으면 우선 사용
    if (statusCode && options.customErrorMessages?.[statusCode]) {
        ElMessage.error(options.customErrorMessages[statusCode])
        return
    }

    // 상태 코드별 기본 처리
    switch (statusCode) {
        case 401:
            ElMessage.error('로그인이 필요합니다')
            if (!options.skipRedirect) {
                router.push(options.unauthorizedRedirect || '/login')
            }
            break

        case 403:
            ElMessage.error('권한이 없습니다')
            break

        case 404:
            ElMessage.error('데이터를 찾을 수 없습니다')
            if (!options.skipRedirect) {
                router.push(options.notFoundRedirect || '/posts')
            }
            break

        case 500:
            ElMessage.error('서버 오류가 발생했습니다')
            break

        default:
            // 네트워크 에러나 기타 에러
            if (!error.response) {
                // 응답 자체가 없음 = 네트워크 문제
                if (error.code === 'NETWORK_ERROR') {
                    ElMessage.error('네트워크 연결을 확인해주세요')
                } else if (error.code === 'ECONNABORTED') {
                    ElMessage.error('요청 시간이 초과되었습니다')
                } else {
                    ElMessage.error('서버에 연결할 수 없습니다')
                }
            } else {
                // 응답은 왔지만 에러 상태 = 백엔드 에러
                ElMessage.error('요청 처리 중 오류가 발생했습니다')
            }
    }
}

/**
 * 게시글 관련 API 에러 처리
 * @param error - API 에러 객체
 * @param context - 컨텍스트 ('list' | 'detail' | 'create' | 'edit')
 */
export const handlePostApiError = (error: any, context: 'list' | 'detail' | 'create' | 'edit') => {
    const contextMessages = {
        list: '게시글 목록을 불러오는데 실패했습니다',
        detail: '게시글을 불러오는데 실패했습니다',
        create: '게시글 등록에 실패했습니다',
        edit: '게시글 수정에 실패했습니다'
    }

    const redirectPaths = {
        list: '/posts',
        detail: '/posts',
        create: '/posts',
        edit: '/posts'
    }

    handleApiError(error, {
        notFoundRedirect: redirectPaths[context],
        customErrorMessages: {
            500: contextMessages[context]
        }
    })
}

/**
 * 댓글 관련 API 에러 처리
 * @param error - API 에러 객체
 * @param context - 컨텍스트 ('load' | 'create' | 'edit' | 'delete')
 */
export const handleCommentApiError = (error: any, context: 'load' | 'create' | 'edit' | 'delete') => {
    const contextMessages = {
        load: '댓글을 불러오는데 실패했습니다',
        create: '댓글 작성에 실패했습니다',
        edit: '댓글 수정에 실패했습니다',
        delete: '댓글 삭제에 실패했습니다'
    }

    handleApiError(error, {
        skipRedirect: true, // 댓글 에러는 페이지 이동 안함
        customErrorMessages: {
            500: contextMessages[context]
        }
    })
}

/**
 * 인증 관련 API 에러 처리
 * @param error - API 에러 객체
 */
export const handleAuthApiError = (error: any) => {
    handleApiError(error, {
        customErrorMessages: {
            401: '아이디 또는 비밀번호가 틀렸습니다',
            500: '로그인 중 오류가 발생했습니다'
        },
        skipRedirect: true // 로그인 페이지에서는 리다이렉트 안함
    })
}
