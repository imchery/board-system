<template>
  <div class="comment-wrapper" :class="{'is-reply': comment.isReply}">

    <!--  대댓글 연결선  -->
    <div v-if="comment.isReply" class="reply-connector"></div>

    <div class="comment-item">
      <!-- 댓글 헤더 -->
      <div class="comment-header">
        <div class="comment-author-info">
          <el-avatar :size="comment.isReply ? 28 : 32" class="author-avatar">
            <el-icon>
              <User/>
            </el-icon>
          </el-avatar>
          <div class="author-details">
            <span class="author-name">{{ comment.author }}</span>
            <span class="comment-date">{{ formatDate(comment.createdAt) }}</span>
          </div>
        </div>

        <!-- 액션 버튼들 (호버 시 표시) -->
        <div class="comment-actions">
          <!-- 수정 버튼 (작성자만) -->
          <button
              v-if="isAuthor"
              class="action-btn edit-btn"
              @click="toggleEditMode"
              :class="{ active: isEditing }"
          >
            <el-icon>
              <Edit/>
            </el-icon>
          </button>

          <!-- 삭제 버튼 (작성자만) -->
          <button
              v-if="isAuthor"
              class="action-btn delete-btn"
              @click="handleDelete"
          >
            <el-icon>
              <Delete/>
            </el-icon>
          </button>
        </div>
      </div>

      <!-- 댓글 내용 -->
      <div class="comment-content">
        <!-- 일반 모드 -->
        <div v-if="!isEditing" class="content-text">
          {{ comment.content }}
        </div>

        <!-- 수정 모드 -->
        <div v-else class="content-edit">
          <el-input
              v-model="editText"
              type="textarea"
              :rows="2"
              maxlength="1000"
              show-word-limit
              class="edit-textarea"
          />
          <div class="edit-actions">
            <button class="cancel-btn" @click="cancelEdit">취소</button>
            <button
                class="save-btn"
                @click="saveEdit"
                :disabled="editLoading || !editText.trim()"
            >
              <span v-if="editLoading">저장 중...</span>
              <span v-else>저장</span>
            </button>
          </div>
        </div>
      </div>

      <!-- 댓글 하단 액션바 -->
      <div v-if="!isEditing" class="comment-footer">
        <!-- 답글 버튼 (최상위 댓글만) -->
        <button
            v-if="!comment.isReply && authStore.isLoggedIn"
            class="reply-btn"
            @click="toggleReplyForm"
        >
          <el-icon>
            <ChatLineRound/>
          </el-icon>
          답글
        </button>

        <!-- 답글 개수 표시 -->
        <div v-if="!comment.isReply && replyCount > 0" class="reply-summary">
          <span class="reply-count">답글 {{ replyCount }}개</span>
        </div>
      </div>

      <!-- 답글 작성 폼 -->
      <div v-if="showReplyForm" class="reply-form-container">
        <div class="reply-form-header">
          <span class="reply-to">{{ comment.author }}님에게 답글</span>
          <button class="close-btn" @click="hideReplyForm">
            <el-icon>×</el-icon>
          </button>
        </div>
        <CommentForm
            :post-id="comment.postId"
            :parent-comment-id="comment.id"
            :is-reply="true"
            @success="handleReplySuccess"
            @cancel="hideReplyForm"
        />
      </div>
    </div>

    <!--  대댓글 목록  -->
    <div v-if="!comment.isReply && (replyPreview.length > 0 || showReplies)" class="reply-preview">
      <!--    답글 미리보기 또는 전체 목록    -->
      <div v-if="!showReplies && replyPreview.length > 0" class="preview-replies">
        <button class="show-replies-btn" @click="toggleReplies">
          <el-icon>
            <ArrowDown/>
          </el-icon>
          답글 {{ replyCount }}개 보기
        </button>

        <!--    첫 번째 답글만 미리보기    -->
        <CommentItem
            v-if="replyPreview[0]"
            :key="replyPreview[0].id"
            :comment="replyPreview[0]"
            @updated="$emit('updated')"
            @deleted="$emit('deleted')"
        />
      </div>

      <!--   전체 답글 목록   -->
      <div v-else-if="showReplies" class="full-replies">
        <button class="hide-replies-btn" @click="toggleReplies">
          <el-icon>
            <ArrowUp/>
          </el-icon>
          답글 숨기기
        </button>

        <div class="replies-list">
          <CommentItem
              v-for="reply in replies"
              :key="reply.id"
              :comment="reply"
              @updated="$emit('updated')"
              @deleted="$emit('deleted')"
          />
        </div>

        <!--   답글 더보기    -->
        <div v-if="hasMoreReplies" class="load-more-replies">
          <button
              class="load-more-btn"
              @click="loadMoreReplies"
              :disabled="repliesLoading"
          >
            <span v-if="repliesLoading">로딩 중...</span>
            <span v-else>답글 더보기</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {CommentResponse, CommentUpdateRequest} from "@/types/api.ts";
import {useAuthStore} from "@/stores/auth.ts";
import {computed, onMounted, ref} from "vue";
import {ArrowDown, ArrowUp, ChatLineRound, Delete, Edit, User} from "@element-plus/icons-vue";
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
const showReplyForm = ref(false) // 댓글 작성 폼 보임/숨김
const isEditing = ref(false) // 댓글 수정 모드 on/off
const showReplies = ref(false) // 답글 목록 펼침/접기

const editText = ref('') // 수정할 댓글 내용
const editLoading = ref(false) // 수정 버튼 로딩 상태

const replyPreview = ref<CommentResponse[]>([]) // 답글 미리보기(3개)
const replies = ref<CommentResponse[]>([]) // 전체 답글 목록
const replyCount = ref(0) // 답글 개수
const repliesLoading = ref(false) // 답글 로딩 상태
const hasMoreReplies = ref(false) // 더 불러올 답글 있는지 확인
const currentReplyPage = ref(0) // 현재 답글 페이지

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
