<template>
  <div class="comment-wrapper" :class="{'is-reply': comment.isReply}">
    <div class="comment-item">

      <!-- 댓글 헤더: 프로필(왼쪽) + 날짜/액션버튼(오른쪽) -->
      <div class="comment-header">
        <div class="comment-author">
          <el-avatar :size="comment.isReply ? 24 : 28" class="comment-avatar">
            <el-icon>
              <User/>
            </el-icon>
          </el-avatar>
          <div class="comment-info">
            <span class="comment-author-name">{{ comment.author }}</span>
          </div>
        </div>

        <!--    오른쪽 영역: 날짜 + 액션 + 좋아요    -->
        <div class="comment-right-section">
          <!--     첫 번째 줄: 날짜 + 수정/삭제 드롭다운     -->
          <div class="comment-meta-line">
            <span class="comment-date">{{ formatDate(comment.createdAt) }}</span>

            <!-- 수정/삭제 드롭다운 -->
            <div class="comment-actions" v-if="isAuthor && !isEditing">
              <el-dropdown trigger="click" @command="handleCommand">
                <el-button
                    size="small"
                    text
                    circle
                    class="comment-more-btn"
                >
                  <el-icon>
                    <MoreFilled/>
                  </el-icon>
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

          <!--     두 번째 줄: 좋아요 버튼(오른쪽 정렬)     -->
          <div v-if="!isEditing" class="comment-like-section">
            <CommentLikeButton
                :comment-id="comment.id"
                :initial-like-count="comment.likeCount || 0"
                :initial-is-liked="comment.isLikedByCurrentUser || false"
                :is-authenticated="isLoggedIn"
                @like-toggled="handleLikeToggled"
            />
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

      <!-- 답글 작성 폼 -->
      <div v-if="!comment.isReply && showReplyForm" class="reply-form">
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

    <!-- 댓글 하단 액션바 (답글 버튼들은 기존 위치 유지) -->
    <div v-if="!isEditing" class="comment-footer">
      <div class="comment-left-actions">
        <!-- 답글 버튼 (최상위 댓글만) -->
        <el-button
            v-if="!comment.isReply  && !showReplyForm"
            size="small"
            text
            @click="toggleReplyForm"
            class="reply-btn"
        >
          <template #icon>
            <el-icon>
              <ChatLineRound/>
            </el-icon>
          </template>
          답글
        </el-button>
      </div>

      <!-- 답글 개수 및 토글 (답글이 있는 경우만) -->
      <div v-if="!comment.isReply && replyCount > 0" class="comment-right-actions">
        <el-button
            size="small"
            text
            @click="toggleReplies"
            class="replies-toggle"
        >
          <template #icon>
            <el-icon>
              <ArrowDown v-if="!showReplies"/>
              <ArrowUp v-else/>
            </el-icon>
          </template>
          {{ showReplies ? '답글 숨기기' : `답글 ${replyCount}개 보기` }}
        </el-button>
      </div>
    </div>

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
</template>

<script setup lang="ts">
import {CommentResponse, CommentUpdateRequest} from "@/types/api.ts";
import {useAuthStore} from "@/stores/auth.ts";
import {computed, onMounted, ref} from "vue";
import {ArrowDown, ArrowUp, ChatLineRound, Delete, Edit, MoreFilled, User} from "@element-plus/icons-vue";
import CommentForm from "@/components/comment/CommentForm.vue";
import {ElMessage, ElMessageBox} from "element-plus";
import {commentApi} from "@/api/comment.ts";
import {formatDate} from "@/utils/dateFormat.ts";
import {handleCommentApiError} from "@/utils/errorHandler.ts";
import CommentLikeButton from "@/components/comment/CommentLikeButton.vue";

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

const isLoggedIn = computed(() => {
  return authStore.isLoggedIn
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
    handleCommentApiError(error, 'edit')
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
      handleCommentApiError(error, 'delete')
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
    handleCommentApiError(error, 'load')
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
    handleCommentApiError(error, 'load')
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
    handleCommentApiError(error, 'load')
  } finally {
    repliesLoading.value = false
  }
}

/**
 * 댓글 좋아요 토글 이벤트 처리
 * @param commentId 댓글 ID
 * @param newLikeCount 변경된 좋아요 개수
 * @param newIsLiked 변경된 좋아요 상태
 */
const handleLikeToggled = async (commentId: string, newLikeCount: number, newIsLiked: boolean) => {
  try {
    console.log('댓글 좋아요 토글 처리:', {commentId, newLikeCount, newIsLiked})

    // 실제 API 호출
    const response = await commentApi.toggleLike(commentId)

    if (response.result) {
      // 성공 시 로그
      console.log('댓글 좋아요 API 성공:', response.data)

      // 서버 응답과 클라이언트 상태 동기화 검증
      const serverLikeCount = response.data.likeCount
      const serverIsLiked = response.data.isLikedByCurrentUser

      if (serverLikeCount !== newLikeCount || serverIsLiked !== newIsLiked) {
        console.warn('클라이언트-서버 상태 불일치 감지:', {
          client: {newLikeCount, newIsLiked},
          server: {serverLikeCount, serverIsLiked},
        })

        // 상태 강제 동기화 (부모 컴포넌트에 알림)
        emit('updated')
      }
    } else {
      ElMessage.error(response.message || '좋아요 처리에 실패했습니다.')
      // 실패 시 댓글 목록 새로고침으로 상태 복구
      emit('updated')
    }
  } catch
      (error) {
    console.error('댓글 좋아요 API 에러:', error)
    ElMessage.error('네트워크 오류가 발생했습니다. 다시 시도해주세요.')
    emit('updated')
  }
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
