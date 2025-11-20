/**
 * 인증 코드 발송 쿨다운 관리 유틸리티
 * LocalStorage를 사용하여 이메일별 마지막 발송 시간 추적
 */
const COOLDOWN_MINUTES = 3
const STORAGE_PREFIX = 'verification:lastSent:'

export type VerificationCodeType = 'findUsername' | 'resetPassword' | 'signup'

/**
 * 인증 코드 재발송 가능 여부 확인
 * @param email 이메일 주소
 * @param type 인증 코드 타입
 */
export const canSendVerificationCode = (email: string, type: VerificationCodeType): {
    canSend: boolean
    remainingSeconds: number
} => {
    const key = `${STORAGE_PREFIX}${type}:${email}`
    const lastSentKey = localStorage.getItem(key)

    if (!lastSentKey) {
        return {canSend: true, remainingSeconds: 0}
    }

    try {
        // 문자열 -> Date 객체 변환
        const lastSent = new Date(lastSentKey)
        const now = new Date();

        // 시간 차이 계산(밀리초 -> 초)
        const diffMs = now.getTime() - lastSent.getTime();
        const diffSeconds = Math.floor(diffMs / 1000);
        const cooldownSeconds = COOLDOWN_MINUTES * 60

        if (diffSeconds >= cooldownSeconds) {
            // 쿨다운 종료 -> 발송가능
            return {canSend: true, remainingSeconds: 0}
        } else {
            // 쿨다운 진행중 -> 발송 불가
            const remainingSeconds = cooldownSeconds - diffSeconds
            return {canSend: false, remainingSeconds}
        }
    } catch (error) {
        // 날짜 파싱 실패 -> 안전하게 발송 가능으로 처리
        console.error('마지막 발송 시간 파싱 실패:', error)
        return {canSend: true, remainingSeconds: 0}
    }
}

/**
 * 인증 코드 발송 시간 기록
 * @param email 이메일 주소
 * @param type 인증 코드 타입
 */
export const recordVerificationCodeSent = (email: string, type: VerificationCodeType): void => {
    const key = `${STORAGE_PREFIX}${type}:${email}`
    const now = new Date().toISOString()
    localStorage.setItem(key, now)

    console.log(`인증 코드 발송 기록 - type: ${type}, email: ${email}`)
}

/**
 * 남은 시간을 "분:초" 형식으로 변환
 * @param seconds 남은 초
 */
export const formatRemainingTime = (seconds: number): string => {
    const minutes = Math.floor(seconds / 60)
    const secs = seconds % 60

    if (minutes > 0) {
        return `${minutes}분 ${secs}초`
    } else {
        return `${secs}초`
    }
}
