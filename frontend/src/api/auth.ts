import axios from "axios";
import {
    FindUserResponse,
    LoginRequest,
    LoginResponse,
    ResetPasswordRequest,
    ResetPasswordResponse,
    ResponseVO,
    SignupRequest,
    SignupResponse,
    VerifyAccountRequest,
    VerifyEmailRequest
} from "@/types/api.ts";

// 백엔드 API 기본 설정
const API_BASE_URL = 'http://localhost:9081';

// Axios 인스턴스 생성
const authClient = axios.create({
    baseURL: API_BASE_URL,
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json'
    }
})

// 응답 인터셉터 (에러 처리)
authClient.interceptors.response.use(
    (response) => {
        return response.data // 응답 데이터만 반환
    },
    (error) => {
        console.error('API 에러:', error)
        return Promise.reject(error)
    }
)

// 인증 관련 API 함수들
export const authApi = {
    // 로그인
    login: async (loginData: LoginRequest): Promise<LoginResponse> => {
        return await authClient.post('/auth/login', loginData)
    },

    // 아이디 찾기
    findUsername: async (
        email: string,
        verificationCode: string
    ): Promise<ResponseVO<FindUserResponse>> => {
        return await authClient.post('/auth/find/username', {
            email, verificationCode
        })
    },

    // 토큰 검증
    validateToken: async (token: string): Promise<boolean> => {
        return await authClient.get('/auth/validate', {
            headers: {
                Authorization: `Bearer ${token}`
            }
        })
    },

    // 토큰에서 사용자 정보 추출
    getUserFromToken: async (token: string): Promise<string> => {
        return await authClient.get('/auth/user', {
            headers: {
                Authorization: `Bearer ${token}`
            }
        })
    },

    // 헬스 체크
    health: async (): Promise<string> => {
        return await authClient.get('/auth/health')
    },

    // ===== 회원가입 관련 =====

    /**
     * 회원가입
     * @param signupData
     */
    signup: async (signupData: SignupRequest): Promise<ResponseVO<SignupResponse>> => {
        return await authClient.post('/auth/signup', signupData)
    },

    /**
     * 아이디 중복 체크
     * true: 사용가능, false: 이미 사용 중
     * @param username 확인할 아이디
     */
    checkUsername: async (username: string): Promise<boolean> => {
        const response: ResponseVO<boolean> = await authClient.get(
            `/auth/check/username?username=${username}`
        )
        return response.data
    },

    /**
     * 이메일 중복 체크
     * true: 사용가능, false: 이미 사용 중
     * @param email 확인할 이메일
     */
    checkEmail: async (email: string): Promise<boolean> => {
        const response: ResponseVO<boolean> = await authClient.get(
            `/auth/check/email?email=${email}`
        )
        return response.data
    },

    /**
     * 이메일 인증 코드 발송
     * @param email 인증받을 이메일
     */
    sendVerificationCode: async (email: string): Promise<ResponseVO<void>> => {
        return await authClient.post('/auth/email/send', {email})
    },

    /**
     * 이메일 인증 코드 검증
     * @param email 이메일
     * @param code 6자리 인증코드
     */
    verifyCode: async (email: string, code: string): Promise<ResponseVO<boolean>> => {
        return await authClient.post('/auth/email/verify', {email, code})
    },

    /**
     * 비밀번호 재설정 (임시 비밀번호 발급)
     * @param data 재설정 요청 데이터
     */
    resetPassword: async (data: ResetPasswordRequest): Promise<ResponseVO<ResetPasswordResponse>> => {
        return await authClient.post('/auth/email/reset-password', data)
    },

    /**
     * 아이디/이메일 확인 (비밀번호 재설정용)
     * @param data 확인 요청 데이터
     */
    verifyAccount: async (data: VerifyAccountRequest): Promise<ResponseVO<boolean>> => {
        return await authClient.post('/auth/verify-account', data)
    },

    /**
     * 이메일 확인(아이디 찾기용)
     * @param data 확인 요청 데이터
     */
    verifyEmail: async (data: VerifyEmailRequest): Promise<ResponseVO<boolean>> => {
        return await authClient.post('/auth/verify-email', data)
    }
}

