import axios from "axios";
import {LoginRequest, LoginResponse, ResponseVO, SignupRequest, SignupResponse} from "@/types/api.ts";

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
        const resposne: ResponseVO<boolean> = await authClient.get(
            `/auth/check/email?email=${email}`
        )
        return resposne.data
    }

}

