/**
 * 날짜를 사용자 친화적인 형식으로 포맷팅
 * @param date
 */
export const formatDate = (date: Date | string): string => {
    try {
        const dateObj = typeof date === 'string' ? new Date(date) : date

        // 유효하지 않은 날짜 체크
        if (isNaN(dateObj.getTime())) {
            return '날짜 오류'
        }

        const now = new Date()
        const diff = now.getTime() - dateObj.getTime();
        const days = Math.floor((diff / 1000 * 60 * 60 * 24))

        // 상대적 시간 표시
        if (days === 0) return '오늘'
        if (days === 1) return '어제'
        if (days < 7) return `${days}일 전`
        if (days < 30) return `${Math.floor(days / 7)}주 전`

        // 절대적 시간 표시
        return dateObj.toLocaleDateString('ko-KR', {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
        })
    } catch (error) {
        console.error('날짜 포맷팅 오류:', error)
        return '날짜 오류'
    }
}

/**
 * 상세한 날짜 시간 포맷팅 (미리보기에서 사용)
 * @param date
 */
export const formatDateTime = (date: Date | string): string => {
    try {
        const dateObj = typeof date === 'string' ? new Date(date) : date

        if (isNaN(dateObj.getTime())) {
            return '날짜 오류'
        }

        return dateObj.toLocaleDateString('ko-KR', {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
        })
    } catch (error) {
        console.error('날짜 시간 포맷팅 오류:', error)
        return '날짜 오류'
    }
}
