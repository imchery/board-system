import {defineStore} from 'pinia'
import {computed, readonly, ref} from 'vue'
import {authApi} from '@/api/auth'
import type {LoginRequest} from '@/types/api'

export const useAuthStore = defineStore('auth', () => {
    // ===== State =====
    const token = ref<string | null>(null)
    const username = ref<string | null>(null)
    const isLoading = ref<boolean>(false)

    // ===== Getters =====
    const isLoggedIn = computed(() => {
        return !!token.value && !!username.value
    })

    const currentUser = computed(() => {
        return username.value
    })

    // ===== Actions =====

    /**
     * localStorage에서 토큰 확인하고 인증 상태 복원
     */
    const initializeAuth = async () => {
        console.log('인증 상태 초기화 시작')

        const storedToken = localStorage.getItem('token')
        const storedUsername = localStorage.getItem('username')

        if (storedToken && storedUsername) {
            try {
                // 토큰이 아직 유효한지 확인
                const isValid = await authApi.validateToken(storedToken)

                if (isValid) {
                    // 유효하면 상태 복원
                    token.value = storedToken
                    username.value = storedUsername
                    console.log('토큰 유효, 로그인 상태 복원:', storedUsername)
                } else {
                    // 무효하면 정리
                    clearAuth()
                    console.log('토큰 무효, 로그인 상태 초기화')
                }
            } catch (error) {
                console.error('토큰 검증 실패:', error)
                clearAuth()
            }
        } else {
            console.log('저장된 토큰 없음')
        }
    }

    /**
     * 로그인 처리
     */
    const login = async (loginData: LoginRequest) => {
        isLoading.value = true

        try {
            const response = await authApi.login(loginData)

            if (response.token && response.username) {
                // 상태 업데이트
                token.value = response.token
                username.value = response.username

                // localStorage 저장
                localStorage.setItem('token', response.token)
                localStorage.setItem('username', response.username)

                console.log('로그인 성공:', response.username)
                return {success: true, message: response.message}
            } else {
                return {success: false, message: response.message || '로그인에 실패했습니다'}
            }
        } catch (error) {
            console.error('로그인 에러:', error)
            return {success: false, message: '로그인 중 오류가 발생했습니다'}
        } finally {
            isLoading.value = false
        }
    }

    /**
     * 로그아웃 처리
     */
    const logout = () => {
        console.log('로그아웃 처리')
        clearAuth()
    }

    /**
     * 인증 정보 완전 정리
     */
    const clearAuth = () => {
        token.value = null
        username.value = null
        localStorage.removeItem('token')
        localStorage.removeItem('username')
        console.log('인증 정보 정리 완료')
    }

    /**
     * 토큰 갱신 (나중에 필요하면 구현)
     */
    const refreshToken = async () => {
        // TODO: refresh token 로직 (필요시 구현)
    }

    // ===== Return =====
    return {
        // State
        token: readonly(token),
        username: readonly(username),
        isLoading: readonly(isLoading),

        // Getters
        isLoggedIn,
        currentUser,

        // Actions
        initializeAuth,
        login,
        logout,
        clearAuth,
        refreshToken
    }
})

// 타입 정의 (다른 컴포넌트에서 사용할 때 자동완성용)
export type AuthStore = ReturnType<typeof useAuthStore>
