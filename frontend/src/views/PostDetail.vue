<template>
  <div class="post-detail-container">
    <!-- 로딩 상태 -->
    <div v-if="loading" class="loading-wrapper">
      <el-skeleton animated>
        <template #template>
          <div class="skeleton-header">
            <el-skeleton-item variant="h1" style="width: 60%"/>
            <el-skeleton-item variant="text" style="width: 40%"/>
          </div>
          <el-skeleton-item variant="rect" style="width: 100%; height: 400px; margin-top: 20px;"/>
        </template>
      </el-skeleton>
    </div>

    <!-- 게시글 내용 -->
    <div v-else-if="post" class="post-detail">
      <!-- 헤더 카드 -->
      <el-card class="post-header-card">
        <div class="post-header">
          <div class="post-meta">
            <el-tag v-if="post.category" class="category-tag" type="primary">
              {{ post.category }}
            </el-tag>
            <h1 class="post-title">{{ post.title }}</h1>
            <div class="post-info">
              <div class="author-info">
                <el-avatar :size="32" class="author-avatar">
                  <el-icon>
                    <User/>
                  </el-icon>
                </el-avatar>
                <span class="author-name">{{ post.author }}</span>
              </div>
              <div class="post-stats">
                <span class="post-date">{{ formatDate(post.createdAt) }}</span>
                <span class="stat-item">
                  <el-icon><View/></el-icon>
                  {{ post.viewCount || 0 }}
                </span>
                <span class="stat-item">
                  <el-icon><ChatLineRound/></el-icon>
                  {{ post.commentCount || 0 }}
                </span>
              </div>
            </div>
          </div>

          <!-- 액션 버튼들 -->
          <div class="post-actions">
            <el-button
                :type="isLiked ? 'danger' : 'default'"
                :icon="StarFilled"
                @click="toggleLike"
                :loading="likeLoading"
                class="like-btn"
            >
              {{ post.likeCount || 0 }}
            </el-button>

            <!-- 작성자만 보이는 수정/삭제 버튼 -->
            <div v-if="isAuthor" class="author-actions">
              <el-button
                  type="primary"
                  :icon="Edit"
                  @click="goToEdit"
              >
                수정
              </el-button>
              <el-button
                  type="danger"
                  :icon="Delete"
                  @click="handleDelete"
                  :loading="deleteLoading"
              >
                삭제
              </el-button>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 본문 카드 -->
      <el-card class="post-content-card">
        <div class="post-content" v-html="post.content"></div>
      </el-card>

      <!-- 댓글 섹션 -->
      <el-card class="comments-card">
        <template #header>
          <div class="comments-header">
            <h3>댓글 {{ post.commentCount || 0 }}개</h3>
          </div>
        </template>

        <!-- 댓글 작성 폼 (로그인한 경우만) -->
        <div v-if="authStore.isLoggedIn" class="comment-form">
          <el-input
              v-model="newComment"
              type="textarea"
              :rows="3"
              placeholder="댓글을 입력하세요..."
              maxlength="500"
              show-word-limit
              class="comment-input"
          />
          <div class="comment-form-actions">
            <el-button
                type="primary"
                @click="submitComment"
                :loading="commentLoading"
                :disabled="!newComment.trim()"
            >
              댓글 작성
            </el-button>
          </div>
        </div>

        <!-- 로그인 안내 -->
        <div v-else class="login-prompt">
          <el-alert
              title="댓글을 작성하려면 로그인이 필요합니다"
              type="info"
              :closable="false"
              show-icon
          >
            <template #default>
              <el-button type="primary" size="small" @click="goToLogin">
                로그인하기
              </el-button>
            </template>
          </el-alert>
        </div>

        <!-- 댓글 목록 -->
        <div class="comments-list">
          <div v-if="comments.length === 0" class="no-comments">
            <el-empty description="첫 번째 댓글을 작성해보세요!"/>
          </div>

          <div v-else>
            <div
                v-for="comment in comments"
                :key="comment.id"
                class="comment-item"
            >
              <div class="comment-header">
                <div class="comment-author">
                  <el-avatar :size="24">
                    <el-icon>
                      <User/>
                    </el-icon>
                  </el-avatar>
                  <span class="comment-author-name">{{ comment.author }}</span>
                  <span class="comment-date">{{ formatDate(comment.createdAt) }}</span>
                </div>

                <!-- 댓글 작성자만 보이는 삭제 버튼 -->
                <el-button
                    v-if="comment.author === authStore.currentUser"
                    size="small"
                    type="danger"
                    text
                    :icon="Delete"
                    @click="deleteComment(comment.id)"
                />
              </div>
              <div class="comment-content">{{ comment.content }}</div>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 하단 네비게이션 -->
      <div class="bottom-navigation">
        <el-button @click="goBack" :icon="ArrowLeft">
          목록으로
        </el-button>
        <el-button type="primary" @click="goToCreate" :icon="Edit">
          글쓰기
        </el-button>
      </div>
    </div>

    <!-- 에러 상태 -->
    <div v-else class="error-state">
      <el-result
          icon="error"
          title="게시글을 불러올 수 없습니다"
          sub-title="게시글이 삭제되었거나 존재하지 않습니다"
      >
        <template #extra>
          <el-button type="primary" @click="goBack">목록으로 돌아가기</el-button>
        </template>
      </el-result>
    </div>
  </div>
</template>

<script setup lang="ts">
import {computed, onMounted, ref} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage, ElMessageBox} from 'element-plus'
import {ArrowLeft, ChatLineRound, Delete, Edit, StarFilled, User, View} from '@element-plus/icons-vue'
import {useAuthStore} from '@/stores/auth'
import {postApi} from '@/api/post'
import type {CommentResponse, PostResponse} from '@/types/api'

// Props (라우터에서 전달)
const props = defineProps<{
  id: string
}>()

// 라우터 및 스토어
const router = useRouter()
const authStore = useAuthStore()

// 반응형 데이터 (타입 명시)
const loading = ref<boolean>(true)
const post = ref<PostResponse | null>(null)
const comments = ref<CommentResponse[]>([])
const newComment = ref<string>('')
const isLiked = ref<boolean>(false)
const likeLoading = ref<boolean>(false)
const commentLoading = ref<boolean>(false)
const deleteLoading = ref<boolean>(false)

// 계산된 속성
const isAuthor = computed(() => {
  if (!authStore.isLoggedIn || !post.value) return false
  return authStore.currentUser === post.value.author
})

// 게시글 상세 조회
const fetchPost = async () => {
  try {
    loading.value = true

    const response = await postApi.getPost(props.id)

    if (response.result) {
      post.value = response.data
      // TODO: 댓글 목록도 함께 조회
      // comments.value = response.data.comments || []
    } else {
      ElMessage.error(response.message || '게시글을 불러오는데 실패했습니다')
    }
  } catch (error: any) {
    console.error('게시글 조회 실패:', error)

    if (error.response?.status === 404) {
      ElMessage.error('존재하지 않는 게시글입니다')
    } else if (error.response?.status === 401) {
      ElMessage.error('로그인이 필요합니다')
      router.push('/login')
    } else {
      ElMessage.error('게시글을 불러오는데 실패했습니다')
    }
  } finally {
    loading.value = false
  }
}

// 좋아요 토글
const toggleLike = async () => {
  if (!authStore.isLoggedIn) {
    ElMessage.warning('로그인이 필요합니다')
    router.push('/login')
    return
  }

  try {
    likeLoading.value = true

    const response = await postApi.toggleLike(props.id)

    if (response.result) {
      post.value = response.data
      isLiked.value = !isLiked.value
      ElMessage.success(isLiked.value ? '좋아요!' : '좋아요 취소')
    } else {
      ElMessage.error(response.message || '좋아요 처리에 실패했습니다')
    }
  } catch (error) {
    console.error('좋아요 처리 실패:', error)
    ElMessage.error('좋아요 처리 중 오류가 발생했습니다')
  } finally {
    likeLoading.value = false
  }
}

// 댓글 작성
const submitComment = async () => {
  if (!newComment.value.trim()) {
    ElMessage.warning('댓글 내용을 입력해주세요')
    return
  }

  try {
    commentLoading.value = true

    // TODO: 댓글 API 연동
    // const response = await commentApi.createComment(props.id, newComment.value)

    // 임시 처리
    ElMessage.success('댓글이 작성되었습니다')
    newComment.value = ''
    // fetchPost() // 댓글 목록 새로고침

  } catch (error) {
    console.error('댓글 작성 실패:', error)
    ElMessage.error('댓글 작성에 실패했습니다')
  } finally {
    commentLoading.value = false
  }
}

// 댓글 삭제
const deleteComment = async (commentId: string) => {
  try {
    await ElMessageBox.confirm(
        '댓글을 삭제하시겠습니까?',
        '댓글 삭제',
        {
          confirmButtonText: '삭제',
          cancelButtonText: '취소',
          type: 'warning',
        }
    )

    // TODO: 댓글 삭제 API 연동
    ElMessage.success('댓글이 삭제되었습니다')
    // fetchPost() // 댓글 목록 새로고침

  } catch (error) {
    if (error !== 'cancel') {
      console.error('댓글 삭제 실패:', error)
      ElMessage.error('댓글 삭제에 실패했습니다')
    }
  }
}

// 게시글 삭제
const handleDelete = async () => {
  try {
    await ElMessageBox.confirm(
        '게시글을 삭제하시겠습니까? 삭제된 게시글은 복구할 수 없습니다.',
        '게시글 삭제',
        {
          confirmButtonText: '삭제',
          cancelButtonText: '취소',
          type: 'warning',
        }
    )

    deleteLoading.value = true

    const response = await postApi.deletePost(props.id)

    if (response.result) {
      ElMessage.success('게시글이 삭제되었습니다')
      router.push('/posts')
    } else {
      ElMessage.error(response.message || '게시글 삭제에 실패했습니다')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('게시글 삭제 실패:', error)
      ElMessage.error('게시글 삭제 중 오류가 발생했습니다')
    }
  } finally {
    deleteLoading.value = false
  }
}

// 네비게이션 메서드들
const goBack = () => {
  router.push('/posts')
}

const goToEdit = () => {
  router.push(`/posts/${props.id}/edit`)
}

const goToCreate = () => {
  router.push('/posts/create')
}

const goToLogin = () => {
  router.push('/login')
}

// 날짜 포맷팅 (PostList와 동일한 로직)
const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (days === 0) {
    return '오늘'
  } else if (days === 1) {
    return '어제'
  } else if (days < 7) {
    return `${days}일 전`
  } else {
    return date.toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    })
  }
}

// 컴포넌트 마운트 시 데이터 로드
onMounted(() => {
  fetchPost()
})
</script>

<style scoped>
/* 외부 CSS 파일 import */
@import '@/assets/styles/components/post-detail.css';
</style>
