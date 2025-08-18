import {onBeforeUnmount, onMounted} from "vue";

/**
 * 페이지 이탈 방지 Composable
 * 변경사항이 있을 때 브라우저 새로고침/닫기를 방지
 */
export const usePageLeaveGuard = (hasChanges: () => boolean) => {
    const handleBeforeUnload = (e: BeforeUnloadEvent) => {
        if (hasChanges()) {
            // 브라우저 기본 확인 다이얼로그 표시
            e.preventDefault()
            return ''
        }
    }

    // 컴포넌트 마운트 시 이벤트 리스너 등록
    onMounted(() => {
        window.addEventListener('beforeunload', handleBeforeUnload)
    })

    // 컴포넌트 언마운트 시 이벤트 리스너 해제
    onBeforeUnmount(() => {
        window.removeEventListener('beforeunload', handleBeforeUnload)
    })

    // 수동으로 해제할 수 있는 cleanup 함수 반환
    const cleanup = () => {
        window.removeEventListener('beforeunload', handleBeforeUnload)
    }

    return {cleanup}
}

/**
 * Vue Router용 페이지 이탈 방지
 * 라우터 네비게이션 시에도 확인 다이얼로그 표시
 * @param hasChanges
 * @param router
 * @param message
 */
export const useRouterLeaveGuard = (
    hasChanges: () => boolean,
    router: any,
    message = '저장하지 않은 변경사항이 있습니다. 정말 나가시겠습니까?'
) => {
    // 브라우저 새로고침/닫기 방지
    usePageLeaveGuard(hasChanges)

    // Vue Router 네비게이션 가드
    onMounted(() => {
        const unregister = router.beforeEach((to: any, from: any, next: any) => {
            if (hasChanges()) {
                if (confirm(message)) {
                    next()
                } else {
                    next(false)
                }
            } else {
                next()
            }
        })

        // 컴포넌트 언마운트 시 가드 해제
        onBeforeUnmount(() => {
            unregister()
        })
    })
}
