<template>
  <div class="comment-wrapper" :class="{'is-reply': comment.isReply}">

    <div class="comment-item">
      <!-- 댓글 헤더: 프로필(왼쪽) + 액션버튼(오른쪽 상단) -->
      <div class="comment-header">
        <div class="comment-author">
          <el-avatar :size="comment.isReply ? 24 : 28" class="comment-avatar">
            <el-icon><User/></el-icon>
          </el-avatar>
          <div class="comment-info">
            <span class="comment-author-name">{{ comment.author }}</span>
          </div>
        </div>

        <div class="comment-meta">
          <span class="comment-date">{{ formatDate(comment.createdAt) }}</span>

          <!-- 수정/삭제 드롭다운 (오른쪽 상단) -->
          <div class="comment-actions" v-if="isAuthor && !isEditing">
            <el-dropdown trigger="click" @command="handleCommand">
              <el-button
                  size="small"
                  text
                  circle
                  class="comment-more-btn"
              >
                <el-icon><MoreFilled/></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="edit" :icon="Edit">수정</el-dropdown-item>
                  <el-dropdown-item command="delete" :icon="Delete">삭제</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>

      <!-- 댓글 내용 -->
      <div class="comment-content">
        <!-- 일반 모드 -->
        <div v-if="!isEditing" class="comment-text">
          {{ comment.content }}
        </div>

        <!-- 수정 모드 -->
        <div v-else class="comment-edit-form">
          <el-input
              v-model="editText"
              type="textarea"
              :rows="2"
              maxlength="1000"
              show-word-limit
              class="edit-textarea"
          />
          <div class="comment-edit-actions">
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

      <!-- 답글 버튼 (최상위 댓글만) -->
      <div v-if="!comment.isReply && !isEditing && authStore.isLoggedIn" class="comment-footer">
        <el-button
            size="small"
            text
            @click="toggleReplyForm"
            :icon="ChatLineRound"
            class="reply-btn"
        >
          답글등록
        </el-button>
      </div>

      <!-- 답글 작성 폼 -->
      <div v-if="showReplyForm" class="reply-form">
        <CommentForm
            :post-id="comment.postId"
            :parent-comment-id="comment.id"
            :is-reply="true"
            :comment="comment"
            @success="handleReplySuccess"
            @cancel="hideReplyForm"
        />
      </div>
    </div>

    <!-- 답글 보기/숨기기 토글 -->
    <div v-if="!comment.isReply && replyCount > 0" class="replies-toggle-section">
      <el-button
          v-if="!showReplies"
          size="small"
          text
          @click="toggleReplies"
          class="replies-toggle-btn"
      >
        <el-icon><ArrowDown/></el-icon>
        답글 {{ replyCount }}개 보기
      </el-button>

      <el-button
          v-else
          size="small"
          text
          @click="toggleReplies"
          class="replies-toggle-btn"
      >
        <el-icon><ArrowUp/></el-icon>
        답글 숨기기
      </el-button>
    </div>

    <!-- 답글 목록 -->
    <div v-if="!comment.isReply && showReplies" class="replies-container">
      <!-- 전체 답글 목록만 표시 -->
      <div v-if="replies.length > 0" class="replies-list">
        <CommentItem
            v-for="reply in replies"
            :key="reply.id"
            :comment="reply"
            @updated="$emit('updated')"
            @deleted="$emit('deleted')"
        />

        <!-- 답글 더보기 -->
        <div v-if="hasMoreReplies" class="load-more-section">
          <el-button
              size="small"
              text
              @click="loadMoreReplies"
              :loading="repliesLoading"
          >
            더 많은 답글 보기
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {CommentResponse, CommentUpdateRequest} from "@/types/api.ts";
import {useAuthStore} from "@/stores/auth.ts";
import {computed, onMounted, ref} from "vue";
import {ArrowDown, ArrowUp, ChatLineRound, Delete, Edit, User, MoreFilled} from "@element-plus/icons-vue";
import CommentForm from "@/components/comment/CommentForm.vue";
import {ElMessage, ElMessageBox} from "element-plus";
import {commentApi} from "@/api/comment.ts";

// Props
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
const showReplies = ref(false)

const editText = ref('')
const editLoading = ref(false)

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

// 드롭다운 명령 처리
const handleCommand = (command: string) => {
  if (command === 'edit') {
    toggleEditMode()
  } else if (command === 'delete') {
    handleDelete()
  }
}

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
  loadReplyPreview()
  emit('updated')
}

// 답글 토글 (수정된 부분)
const toggleReplies = () => {
  console.log('toggleReplies 호출됨', {
    showReplies: showReplies.value,
    repliesLength: replies.value.length
  })

  showReplies.value = !showReplies.value

  if (showReplies.value && replies.value.length === 0) {
    console.log('전체 답글 로드 시작')
    loadReplies()
  }
}

// 답글 미리보기 로드
const loadReplyPreview = async () => {
  if (props.comment.isReply) return

  try {
    console.log('답글 미리보기 로드:', props.comment.id)
    const response = await commentApi.getReplyPreview(props.comment.postId, props.comment.id)

    if (response.result) {
      replyPreview.value = response.data
      replyCount.value = response.data.length
      console.log('답글 미리보기 로드 성공:', response.data)
    }
  } catch (error) {
    console.error('답글 미리보기 로드 실패:', error)
  }
}

// 전체 답글 로드
const loadReplies = async () => {
  try {
    console.log('전체 답글 로드 시작')
    repliesLoading.value = true
    currentReplyPage.value = 0

    const response = await commentApi.getReplies(props.comment.postId, props.comment.id, currentReplyPage.value)

    if (response.result) {
      replies.value = response.data.content
      hasMoreReplies.value = !response.data.last
      console.log('전체 답글 로드 성공:', response.data.content)
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
