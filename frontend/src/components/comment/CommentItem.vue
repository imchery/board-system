<template>
  <div class="comment-item" :class="{'is-reply': comment.isReply}">
    <!--  댓글 헤더  -->
    <div class="comment-header">
      <div class="comment-author-info">
        <el-avatar :size="comment.isReply ? 24 : 32" class="author-avatar">
          <el-icon>
            <User/>
          </el-icon>
        </el-avatar>
        <div class="author-details">
          <span class="author-name">{{ comment.author }}</span>
          <span class="comment-date">{{ formatDate(comment.createdAt) }}</span>
        </div>
      </div>

      <!--   액션 버튼들   -->
      <div class="comment-actions">
        <!--    답글 버튼 (최상위 댓글만)    -->
        <el-button
            v-if="!comment.isReply && authStore.isLoggedIn"
            size="small"
            text
            @click="toggleReplyForm"
            :icon="ChatLineRound"
        >
          답글
        </el-button>

        <!--    수정 버튼 (작성자만)    -->
        <el-button
            v-if="isAuthor"
            size="small"
            text
            @click="toggleEditMode"
            :icon="Edit"
        >
          수정
        </el-button>

        <!--    삭제 버튼 (작성자만)    -->
        <el-button
            v-if="isAuthor"
            size="small"
            text
            type="danger"
            @click="handleDelete"
            :icon="Delete"
        >
          삭제
        </el-button>
      </div>
    </div>

    <!--  댓글 내용  -->
    <div class="comment-content">
      <!--   일반 모드   -->
      <div v-if="!isEditing" class="content-text">
        <el-input
            v-model="editText"
            type="textarea"
            :rows="2"
            maxlength="1000"
            show-word-limit
        />
      </div>
      <!--   수정 모드   -->
      <div v-else class="content-edit">
        <el-input
            v-model="editText"
            type="textarea"
            :rows="2"
            maxlength="1000"
            show-word-limit
        />
        <div class="edit-actions">
          <el-button size="small" @click="cancelEdit">취소</el-button>
          <el-button
              size="small"
              type="primary"
              @click="saveEdit"
              :loading="editLoading"
              :disabled="!editText.trim()"
          >
            저장
          </el-button>
        </div>
      </div>
    </div>

    <!--  답글 작성 폼  -->
    <div v-if="showReplyForm" class="reply-form-container">
      <CommentForm
          :post-id="comment.postId"
          :parent-comment-id="comment.id"
          :is-reply="true"
          @success="handleReplySuccess"
          @cancel="hideReplyForm"
      />
    </div>

    <!--  대댓글 미리보기 (최상위 댓글만)  -->
    <div v-if="!comment.isReply && replyPreview.length > 0" class="reply-preview">
      <div class="reply-header">
        <span class="reply-count">답글 {{ replyCount }}개</span>
        <el-button
            size="small"
            text
            @click="toggleReplies"
            :icon="showReplies ? ArrowUp : ArrowDown"
        >
          {{ showReplies ? '접기' : '더보기' }}
        </el-button>
      </div>

      <!--   답글 미리보기   -->
      <div v-if="!showReplies" class="preview-items">
        <CommentItem
            v-for="reply in replyPreview"
            :key="reply.id"
            :comment="reply"
            @updated="$emit('updated')"
            @deleted="$emit('deleted')"
        />
      </div>

      <!--   전체 답글 목록   -->
      <div v-else class="full-replies">
        <CommentItem
            v-for="reply in replies"
            :key="reply.id"
            :comment="reply"
            @updated="$emit('updated')"
            @deleted="$emit('deleted')"
        />

        <!--    답글 페이징    -->
        <div v-if="hasMoreReplies" class="reply-pagination">
          <el-button
              size="small"
              text
              @click="loadMoreReplies"
              :loading="repliesLoading"
          >
            답글 더보기
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// Props
import {CommentResponse, CommentUpdateRequest} from "@/types/api.ts";
import {useAuthStore} from "@/stores/auth.ts";
import {computed, onMounted, ref} from "vue";
import {ArrowDown, ArrowUp, ChatLineRound, Delete, Edit, User} from "@element-plus/icons-vue";
import CommentForm from "@/components/comment/CommentForm.vue";
import {ElMessage, ElMessageBox} from "element-plus";
import {commentApi} from "@/api/comment.ts";

interface Props {
  comment: CommentResponse
}

const props = defineProps<Props>()

// Emits
const emit = defineEmits<{
  updated: []
  deleted: []
}>()

// 스토어
const authStore = useAuthStore()

// 반응형 데이터
const showReplyForm = ref(false)
const isEditing = ref(false)
const editText = ref('')
const editLoading = ref(false)
const showReplies = ref(false)
const replyPreview = ref<CommentResponse[]>([])
const replies = ref<CommentResponse[]>([])
const replyCount = ref(0)
const repliesLoading = ref(false)
const hasMoreReplies = ref(false)
const currentReplyPage = ref(0)

// 계산된 속성
const isAuthor = computed(() => {
  return authStore.isLoggedIn && authStore.currentUser === props.comment.author
})

// 답글 폼 토글
const toggleReplyForm = () => {
  showReplyForm.value = !showReplyForm.value
}

const hideReplyForm = () => {
  showReplyForm.value = false
}

// 수정 모드 토글
const toggleEditMode = () => {
  isEditing.value = !isEditing.value
  if (isEditing.value) {
    editText.value = props.comment.content
  }
}

const cancelEdit = () => {
  isEditing.value = false
  editText.value = ''
}

// 댓글 수정
const saveEdit = async () => {
  if (!editText.value.trim()) {
    ElMessage.warning('댓글 내용을 입력해주세요')
    return
  }
  try {
    editLoading.value = true

    const request: CommentUpdateRequest = {
      content: editText.value.trim(),
    }

    const response = await commentApi.updateComment(props.comment.id, request)

    if (response.result) {
      ElMessage.success('댓글이 수정되었습니다')
      isEditing.value = false
      emit('updated')
    } else {
      ElMessage.error(response.message || '댓글 수정에 실패했습니다')
    }
  } catch (error) {
    console.error('댓글 수정 실패:', error)
    ElMessage.error('댓글 수정 중 오류가 발생했습니다')
  } finally {
    editLoading.value = false
  }
}

// 댓글 삭제
const handleDelete = async () => {
  try {
    await ElMessageBox.confirm(
        '댓글을 삭제하시겠습니까?',
        '댓글 삭제',
        {
          confirmButtonText: '삭제',
          cancelButtonText: '취소',
          type: 'warning'
        }
    )

    const response = await commentApi.deleteComment(props.comment.id)

    if (response.result) {
      ElMessage.success('댓글이 삭제되었습니다')
      emit('deleted')
    } else {
      ElMessage.error(response.message || '댓글 삭제에 실패했습니다')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('댓글 삭제 실패:', error)
      ElMessage.error('댓글 삭제 중 오류가 발생했습니다')
    }
  }
}

// 답글 성공 처리
const handleReplySuccess = () => {
  hideReplyForm()
  loadReplyPreview() // 답글 미리보기 새로고침
  emit('updated')
}

// 답글 토글
const toggleReplies = () => {
  showReplies.value = !showReplies.value
  if (showReplies.value && replies.value.length === 0) {
    loadReplies()
  }
}

// 답글 미리보기 로드
const loadReplyPreview = async () => {
  if (props.comment.isReply) return

  try {
    const response = await commentApi.getReplyPreview(props.comment.postId, props.comment.id)

    if (response.result) {
      replyPreview.value = response.data
      replyCount.value = response.data.length
    }
  } catch (error) {
    console.error('답글 미리보기 로드 실패:', error)
  }
}

// 전체 답글 로드
const loadReplies = async () => {
  try {
    repliesLoading.value = true
    currentReplyPage.value = 0

    const response = await commentApi.getReplies(props.comment.postId, props.comment.id, currentReplyPage.value)

    if (response.result) {
      replies.value = response.data.content
      hasMoreReplies.value = !response.data.last
    }
  } catch (error) {
    console.error('답글 로드 실패:', error)
  } finally {
    repliesLoading.value = false
  }
}

// 답글 더보기
const loadMoreReplies = async () => {
  try {
    repliesLoading.value = true
    currentReplyPage.value++

    const response = await commentApi.getReplies(props.comment.postId, props.comment.id, currentReplyPage.value)

    if (response.result) {
      replies.value.push(...response.data.content)
      hasMoreReplies.value = !response.data.last
    }
  } catch (error) {
    console.error('답글 더보기 실패:', error)
  } finally {
    repliesLoading.value = false
  }
}

// 날짜 포맷팅
const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '방금 전'
  if (minutes < 60) return `${minutes}분 전`
  if (hours < 24) return `${hours}시간 전`
  if (days < 7) return `${days}일 전`

  return date.toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  })
}

// 컴포넌트 마운트 시 답글 미리보기 로드
onMounted(() => {
  if (!props.comment.isReply) {
    loadReplyPreview()
  }
})
</script>

<style scoped>
@import '@/assets/styles/components/comment.css';
</style>
