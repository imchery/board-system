<template>
  <div class="comment-form">
    <!--  답글일 때만 헤더 표시  -->
    <div v-if="isReply" class="form-header">
      <span class="reply-indicator">답글 작성</span>
      <el-button
          size="small"
          text
          @click="cancelReply"
          :icon="Close"
      >
        취소
      </el-button>
    </div>

    <!--  댓글 입력 영역  -->
    <div class="form-content">
      <el-input
          v-model="commentText"
          type="textarea"
          :row="isReply ? 2 : 3"
          :placeholder="isReply ? '답글을 입력하세요...' : '댓글을 입력하세요...'"
          maxlength="1000"
          show-word-limit
          class="comment-textarea"
          @keydown.ctrl.enter="submitComment"
          @keydown.meta.enter="submitComment"
      />

      <div class="form-actions">
        <el-button
            type="primary"
            @click="submitComment"
            :loading="loading"
            :disabled="!commentText.trim()"
            size="small"
        >
          {{ isReply ? '답글 등록' : '댓글 등록' }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">

import {Close} from "@element-plus/icons-vue";
import {ref} from "vue";
import {ElMessage} from "element-plus";
import {CommentRequest} from "@/types/api.ts";
import {commentApi} from "@/api/comment.ts";
import {handleCommentApiError} from "@/utils/errorHandler.ts";

// Props: 전달할 데이터 타입 정의
interface Props {
  postId: string
  parentCommentId: string
  isReply?: boolean
}

// 전달할 데이터(자식은 읽기전용)
const props = withDefaults(defineProps<Props>(), {
  isReply: false,
})

// Emits (성공여부 확인)
const emit = defineEmits<{
  success: [comment: any]
  cancel: []
}>()

// 반응형 데이터
const commentText = ref('')
const loading = ref(false)

// 댓글 작성
const submitComment = async () => {
  if (!commentText.value.trim()) {
    ElMessage.warning('댓글 내용을 입력해주세요')
    return
  }

  try {
    loading.value = true

    const request: CommentRequest = {
      postId: props.postId,
      content: commentText.value.trim(),
      parentCommentId: props.parentCommentId,
    }

    const response = await commentApi.createComment(props.postId, request)

    if (response.result) {
      ElMessage.success(props.isReply ? '답글이 등록되었습니다' : '댓글이 등록되었습니다')
      commentText.value = ''
      emit('success', response.data)
    } else {
      ElMessage.error(response.message || '댓글 등록에 실패했습니다')
    }
  } catch (error) {
    handleCommentApiError(error, 'create')
  } finally {
    loading.value = false
  }
}

// 답글 취소
const cancelReply = () => {
  commentText.value = ''
  emit('cancel')
}
</script>
<style scoped>
@import '@/assets/styles/components/comment.css';
</style>
