export const getTextLength = (htmlContent: string): number => {
    if (!htmlContent?.trim()) return 0

    try {
        // DOM 파싱으로 HTML 태그 제거
        const div = document.createElement('div')
        div.innerHTML = htmlContent

        // 텍스트만 추출하고 앞뒤 공백 제거
        const textContent = div.textContent || div.innerText || ''
        return textContent.trim().length
    } catch (error) {
        console.error('텍스트 길이 계산 오류:', error)
        // 에러 발생 시 대략적인 길이라도 반환
        return htmlContent.replace(/<[^>]*>/g, '').trim().length
    }
}

/**
 * 텍스트 미리보기 생성 (요약용)
 * @param htmlContent
 * @param maxLength
 */
export const getTextPreview = (htmlContent: string, maxLength = 100): string => {
    if (!htmlContent?.trim()) return ''

    try {
        const div = document.createElement('div')
        div.innerHTML = htmlContent
        const textContent = (div.textContent || div.innerText || '').trim()

        if (textContent.length <= maxLength) {
            return textContent
        }

        return textContent.substring(0, maxLength) + '...'
    } catch (error) {
        console.error('텍스트 미리보기 생성 오류:', error)
        const plainText = htmlContent.replace(/<[^>]*>/g, '').trim()
        return plainText.length > maxLength ? plainText.substring(0, maxLength) + '...' : plainText
    }
}

/**
 * 안전한 HTML 변환 (XSS 방지 기본 수준)
 * @param text
 */
export const escapeHtml = (text: string): string => {
    const div = document.createElement('div')
    div.textContent = text
    return div.innerHTML
}

/**
 * 검색 키워드 하이라이트
 * @param text - 원본 텍스트
 * @param keyword - 검색 키워드
 */
export const highlightKeyword = (text: string, keyword: string): string => {
    if (!keyword?.trim()) return text

    const regex = new RegExp(`(${keyword})`, 'gi')
    return text.replace(regex, '<mark>$1</mark>')
}
