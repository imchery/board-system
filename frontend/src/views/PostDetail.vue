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
      <el-card class="unified-post-card">

        <!-- 1. 헤더 섹션 -->
        <div class="post-header-section">
          <div class="post-meta">
            <el-tag v-if="post.category" class="category-tag" type="primary">
              {{ post.category }}
            </el-tag>

            <!-- 제목과 좋아요를 한 줄에 배치 -->
            <div class="title-with-like">
              <h1 class="post-title">{{ post.title }}</h1>
              <button
                  class="like-button"
                  :class="{ active: isLiked }"
                  @click="toggleLike"
                  :disabled="likeLoading"
              >
                <el-icon class="heart-icon">
                  <svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
                    <path
                        d="M923 283.6c-13.4-31.1-32.6-58.9-56.9-82.8-24.3-23.9-52.8-42.6-84.9-55.5C749.3 131.6 714.7 124 679 124c-39.1 0-77.5 9.7-114.3 28.9-33.8 17.6-63.3 42.8-87.7 74.9-24.4-32.1-53.9-57.3-87.7-74.9C352.5 133.7 314.1 124 275 124c-35.7 0-70.3 7.6-102.2 21.3-32.1 12.9-60.6 31.6-84.9 55.5-24.3 23.9-43.5 51.7-56.9 82.8-13.9 32.3-21 66.6-21 101.9 0 33.3 6.8 68 20.3 103.3 11.3 29.5 27.5 60.1 48.2 91 32.8 48.9 77.9 99.9 133.9 151.6 92.8 85.7 184.7 144.9 188.6 147.3l23.7 15.2c10.5 6.7 24 6.7 34.5 0l23.7-15.2c3.9-2.5 95.7-61.6 188.6-147.3 56-51.7 101.1-102.7 133.9-151.6 20.7-30.9 37-61.5 48.2-91 13.5-35.3 20.3-70 20.3-103.3.1-35.3-7-69.6-20.9-101.9z"/>
                  </svg>
                </el-icon>
                <span class="like-count">{{ post.likeCount || 0 }}</span>
              </button>

              <!-- 작성자 전용 드롭다운 메뉴 -->
              <div v-if="isAuthor" class="post-actions">
                <el-dropdown trigger="click" placement="bottom-end">
                  <button class="more-button">
                    <el-icon class="more-icon">
                      <svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
                        <path
                            d="M176 511a56 56 0 1 0 112 0 56 56 0 1 0-112 0zm280 0a56 56 0 1 0 112 0 56 56 0 1 0-112 0zm280 0a56 56 0 1 0 112 0 56 56 0 1 0-112 0z"/>
                      </svg>
                    </el-icon>
                  </button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item @click="goToEdit">
                        <el-icon>
                          <Edit/>
                        </el-icon>
                        수정
                      </el-dropdown-item>
                      <el-dropdown-item @click="handleDelete" divided>
                        <el-icon>
                          <Delete/>
                        </el-icon>
                        삭제
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>

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
        </div>

        <!-- 구분선 -->
        <el-divider/>

        <!-- 2. 본문 섹션 -->
        <div class="post-content-section">
          <div class="post-content" v-html="post.content"></div>
        </div>

        <!-- 구분선 -->
        <el-divider/>

        <!-- 3. 댓글 섹션 - CommentList 컴포넌트로 교체 -->
        <div class="comments-section">
          <CommentList :post-id="props.id"/>
        </div>
      </el-card>

      <!-- 하단 네비게이션 -->
      <div class="bottom-navigation">
        <el-button @click="goBack" :icon="ArrowLeft">
          목록으로
        </el-button>
        <el-button type="primary" @click="goToCreate" :icon="EditPen">
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
import {ArrowLeft, ChatLineRound, Delete, Edit, EditPen, User, View} from '@element-plus/icons-vue'
import {postApi} from '@/api/post'
import {useAuthStore} from '@/stores/auth'
import type {PostResponse} from '@/types/api'
import CommentList from "@/components/comment/CommentList.vue";
import {handleApiError, handlePostApiError} from "@/utils/errorHandler.ts";

// Props
interface Props {
  id: string
}

const props = defineProps<Props>()

// 라우터 및 스토어
const router = useRouter()
const authStore = useAuthStore()

// 게시글 관련 반응형 데이터
const loading = ref<boolean>(true)
const post = ref<PostResponse | null>(null)
const isLiked = ref<boolean>(false)
const likeLoading = ref<boolean>(false)
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
      if (response.data.isLikedByCurrentUser !== undefined) {
        isLiked.value = response.data.isLikedByCurrentUser
      } else {
        isLiked.value = false
      }
    } else {
      ElMessage.error(response.message || '게시글을 불러오는데 실패했습니다')
    }
  } catch (error: any) {
    handlePostApiError(error, 'list')
  } finally {
    loading.value = false
  }
}

// 좋아요 토글
const toggleLike = async () => {
  if (!authStore.isLoggedIn) {
    ElMessage.warning('로그인이 필요합니다')
    await router.push('/login')
    return
  }

  try {
    likeLoading.value = true
    const response = await postApi.toggleLike(props.id)

    if (response.result) {
      post.value = response.data
      if (response.data.isLikedByCurrentUser !== undefined) {
        isLiked.value = response.data.isLikedByCurrentUser
      }
      console.log(`좋아요 토글 후 상태:`, isLiked.value)
    }
    ElMessage.success(isLiked.value ? '좋아요!' : '좋아요 취소')
  } catch (error) {
    handleApiError(error, {
      customErrorMessages: {
        500: '좋아요 처리에 실패했습니다'
      },
      skipRedirect: true
    })
  } finally {
    likeLoading.value = false
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
      await router.push('/posts')
    } else {
      ElMessage.error(response.message || '게시글 삭제에 실패했습니다')
    }
  } catch (error) {
    if (error !== 'cancel') {
      handleApiError(error, {
        customErrorMessages: {
          500: '게시글 삭제 중 오류가 발생했습니다'
        },
        skipRedirect: true
      })
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

// 날짜 포맷팅
const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (days === 0) return '오늘'
  if (days === 1) return '어제'
  if (days < 7) return `${days}일 전`

  return date.toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
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
