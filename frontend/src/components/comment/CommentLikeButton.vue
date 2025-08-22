<template>
  <el-button
      :type="isLiked ? 'danger' : 'default'"
      :loading="loading"
      :disabled="!isAuthenticated"
      class="like-btn"
      size="small"
      @click="handleLikeToggle"
  >
    <!--  하트 아이콘  -->
    <template #icon>
      <el-icon class="custom-heart">
        <svg viewBox="0 0 24 24" width="16" height="16">
          <path
              :fill="isLiked ? '#f56565' : 'none'"
              :stroke="isLiked ? '#f56565' : 'currentColor'"
              stroke-width="2"
              d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"
          />
        </svg>
      </el-icon>
    </template>

    <!--  좋아요 개수 표시  -->
    <span class="like-count">{{ displayLikeCount }}</span>
  </el-button>
</template>
<script setup lang="ts">
import {computed, ref} from "vue";
import {ElMessage} from "element-plus";

interface Props {
  commentId: string         // 댓글 ID
  initialLikeCount: number  // 초기 좋아요 개수
  initialIsLiked: boolean   // 초기 좋아요 상태
  isAuthenticated?: boolean // 로그인 상태 (기본값: false)
}

const props = withDefaults(defineProps<Props>(), {
  isAuthenticated: false
})

interface Emits {
  likeToggled: [commentId: string, newLikeCount: number, newLiked: boolean]
}

const emit = defineEmits<Emits>()

const loading = ref(false)              // API 호출중인지
const likeCount = ref(props.initialLikeCount)  // 현재 좋아요 개수
const isLiked = ref(props.initialIsLiked)      // 현재 좋아요 상태

/**
 * 화면에 표시할 좋아요 개수
 * 0개일 때는 숫자를 표시하지 않음
 */
const displayLikeCount = computed(() => {
  return likeCount.value > 0 ? likeCount.value : ''
})

/**
 * 좋아요 토글 클릭 핸들러
 */
const handleLikeToggle = async () => {
  // 1. 로그인 상태 확인
  if (!props.isAuthenticated) {
    ElMessage.warning('로그인 후 이용해주세요.')
    return
  }

  // 2. 증복 클릭 방지
  if (loading.value) {
    return
  }

  // 원본 값 백업
  const originalIsLiked = isLiked.value
  const originalLikeCount = likeCount.value

  try {
    loading.value = true

    // 3. 낙관적 업데이트 (UI 먼저 변경)
    const newIsLiked = !isLiked.value
    const newLikeCount = newIsLiked ? likeCount.value + 1 : likeCount.value - 1

    // 즉시 UI 업데이트
    isLiked.value = newIsLiked
    likeCount.value = newLikeCount

    // 4. 부모 컴포넌트에 변경사항 알림
    emit('likeToggled', props.commentId, newLikeCount, newIsLiked)

  } catch (error) {
    // 에러 발생 시 원래 상태로 복구
    isLiked.value = originalIsLiked
    likeCount.value = originalLikeCount
    console.error('댓글 좋아요 토글 실패:', error)
    ElMessage.error('좋아요 처리 중 오류가 발생했습니다.')
  } finally {
    loading.value = false
  }
}
</script>
<style scoped>
@import '@/assets/styles/components/comment.css';
</style>
