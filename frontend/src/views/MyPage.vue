<template>
  <div class="my-page">
    <!--  페이지 헤더  -->
    <el-card class="header-card">
      <div class="page-header">
        <h1 class="page-title">마이페이지</h1>
        <p class="page-subtitle">{{ authStore.currentUser }}님의 활동 내역</p>
      </div>
    </el-card>

    <!--  통계 대시보드  -->
    <el-card class="stats-card">
      <h2 class="section-title">활동 통계</h2>
      <div class="stats-grid" v-loading="statsLoading">
        <div class="stat-item">
          <div class="stat-number">{{ userStats.postCount || 0 }}</div>
          <div class="stat-label">작성한 글</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">{{ userStats.commentCount || 0 }}</div>
          <div class="stat-label">작성한 댓글</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">{{ totalActivities }}</div>
          <div class="stat-label">총 활동</div>
        </div>
      </div>
    </el-card>

    <!--  활동 내역 탭  -->
    <el-card class="content-card">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!--    내가 쓴 글 탭    -->
        <el-tab-pane label="내가 쓴 글" name="posts">
          <div class="tab-content">
            <div class="tab-header">
              <span class="tab-count">총 {{ userStats.postCount || 0 }}개</span>
            </div>

            <!--      게시글 목록      -->
            <div v-loading="postsPagination.loading.value">
              <div v-if="myPosts.length > 0" class="posts-list">
                <div
                    v-for="post in myPosts"
                    :key="post.id"
                    class="post-item"
                    @click="goToPost(post.id)"
                >
                  <div class="post-header">
                    <h3 class="post-title">{{ post.title }}</h3>
                    <span class="post-date">{{ formatDate(post.createdAt) }}</span>
                  </div>
                  <div class="post-meta">
                    <span class="meta-item">
                      <el-icon><View/></el-icon>
                      {{ post.viewCount || 0 }}
                    </span>
                    <span class="meta-item">
                      <el-icon><ChatLineRound/></el-icon>
                      {{ post.commentCount || 0 }}
                    </span>
                  </div>
                </div>
              </div>

              <!--       게시글 없음       -->
              <div v-else class="empty-state">
                <el-empty description="작성한 게시글이 없습니다">
                  <el-button type="primary" @click="goToCreate">첫 번째 글 작성하기</el-button>
                </el-empty>
              </div>
            </div>

            <!-- 게시글 페이지네이션 -->
            <div v-if="myPosts.length > 0" class="pagination-wrapper">
              <el-pagination
                  v-model:current-page="postsPagination.currentPage.value"
                  v-model:page-size="postsPagination.pageSize.value"
                  :page-sizes="[10, 20, 50]"
                  :total="postsPagination.totalElements.value"
                  layout="total, sizes, prev, pager, next"
                  @size-change="handlePostsPageSizeChange"
                  @current-change="handlePostsPageChange"
              />
            </div>
          </div>
        </el-tab-pane>

        <!--    내가 쓴 댓글 탭    -->
        <el-tab-pane label="내가 쓴 댓글" name="comments">
          <div class="tab-content">
            <div class="tab-header">
              <span class="tab-count">총 {{ userStats.commentCount || 0 }}개</span>
            </div>

            <!--      댓글 목록      -->
            <div v-loading="commentPagination.loading.value">
              <div v-if="myComments.length > 0" class="comments-list">
                <div
                    v-for="comment in myComments"
                    :key="comment.id"
                    class="comment-item"
                    @click="goToPostFromComment(comment.postId)"
                >
                  <div class="comment-content">{{ comment.content }}</div>
                  <div class="comment-meta">
                    <span class="comment-date">{{ formatDate(comment.createdAt) }}</span>
                    <span class="comment-type">
                      {{ comment.isReply ? '답글' : '댓글' }}
                    </span>
                  </div>
                </div>
              </div>

              <!--       댓글 없음       -->
              <div v-else class="empty-state">
                <el-empty description="작성한 댓글이 없습니다">
                  <el-button type="primary" @click="goToPosts">게시글 보러가기</el-button>
                </el-empty>
              </div>
            </div>

            <!--      댓글 페이지네이션      -->
            <div v-if="myComments.length > 0" class="pagination-wrapper">
              <el-pagination
                  v-model:current-page="commentPagination.currentPage.value"
                  v-model:page-size="commentPagination.pageSize.value"
                  :page-sizes="[5, 10, 15]"
                  :total="commentPagination.totalElements.value"
                  layout="total, sizes, prev, pager, next"
                  @size-change="handleCommentsPageSizeChange"
                  @current-change="handleCommentsPageChange"
              />
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import {useRouter} from "vue-router";
import {useAuthStore} from "@/stores/auth.ts";
import {computed, onMounted, ref} from "vue";
import {CommentResponse, PostResponse, UserStatsResponse} from "@/types/api.ts";
import {formatDate} from "@/utils/dateFormat.ts";
import {userApi} from "@/api/user.ts";
import {ElMessage} from "element-plus";
import {ChatLineRound, View} from "@element-plus/icons-vue";
import {usePagination} from "@/composables/usePagination.ts";

// 라우터 및 스토어
const router = useRouter()
const authStore = useAuthStore()

// 통계 관련 상태
const userStats = ref<UserStatsResponse>({
  username: '',
  postCount: 0,
  commentCount: 0,
})
const statsLoading = ref(false)

// 탭 관련 상태
const activeTab = ref('posts')

// 게시글 관련 상태
const myPosts = ref<PostResponse[]>([])
const postsLoading = ref(false)
const postsCurrentPage = ref(1)
const postsPageSize = ref(10)
const postsTotalElements = ref(0)

// 댓글 관련 상태
const myComments = ref<CommentResponse[]>([])
const commentsLoading = ref(false)
const commentsCurrentPage = ref(1)
const commentsPageSize = ref(10)
const commentsTotalElements = ref(0)

// 페이징
const postsPagination = usePagination(10)
const commentPagination = usePagination(10)


// 계산된 속성
const totalActivities = computed(() => {
  return (userStats.value.postCount || 0) + (userStats.value.commentCount || 0)
})

// ============= API 호출 함수들 ==============

/**
 * 사용자 통계 조회
 */
const fetchUserStats = async () => {
  try {
    statsLoading.value = true
    const response = await userApi.getMyStats()

    if (response.result) {
      userStats.value = response.data
    } else {
      ElMessage.error(response.message || '통계 조회에 실패했습니다')
    }
  } catch (error) {
    console.error('통계 조회 에러:', error)
    ElMessage.error('통계 정보를 불러오는데 실패했습니다')
  } finally {
    statsLoading.value = false
  }
}

/**
 * 내가 쓴 게시글 조회
 */
const fetchMyPosts = async () => {
  await postsPagination.withLoading(async () => {
    const response = await userApi.getMyPosts(
        postsPagination.currentPage.value - 1,
        postsPagination.pageSize.value,
    )

    if (response.result) {
      myPosts.value = response.data.content
      postsPagination.updateFromResponse(response.data)
    }
  })
}

/**
 * 내가 쓴 댓글 조회
 */
const fetchMyComments = async () => {
  await commentPagination.withLoading(async () => {
    const response = await userApi.getMyComments(
        commentPagination.currentPage.value - 1,
        commentPagination.pageSize.value,
    )

    if (response.result) {
      myComments.value = response.data.content
      commentPagination.updateFromResponse(response.data)
    }
  })
}

// ============== 이벤트 핸들러들 ==============

/**
 * 탭 변경 처리
 * @param tabName
 */
const handleTabChange = (tabName: string) => {
  if (tabName === 'posts' && myPosts.value.length === 0) {
    fetchMyPosts()
  } else if (tabName === 'comments' && myComments.value.length === 0) {
    fetchMyComments()
  }
}

/**
 * 게시글 페이지 변경
 * @param page
 */
const handlePostsPageChange = (page: number) => {
  postsPagination.changePage(page, fetchMyPosts)
}

/**
 * 게시글 사이즈 변경
 * @param size
 */
const handlePostsPageSizeChange = (size: number) => {
  postsPagination.changePageSize(size, fetchMyPosts)
}

/**
 * 댓글 페이지 변경
 * @param page
 */
const handleCommentsPageChange = (page: number) => {
  commentPagination.changePage(page, fetchMyComments)
}

/**
 * 댓글 사이즈 변경
 * @param size
 */
const handleCommentsPageSizeChange = (size: number) => {
  commentPagination.changePageSize(size, fetchMyComments)
}

// ============== 네비게이션 함수들 ==============
const goToPost = (postId: string) => {
  router.push(`/posts/${postId}`)
}

const goToCreate = () => {
  router.push('/posts/create')
}

const goToPostFromComment = (postId: string) => {
  router.push(`/posts/${postId}`)
}

const goToPosts = () => {
  router.push('/posts')
}

// ============== 컴포넌트 마운트 ==============
onMounted(async () => {
  // 1. 통계 먼저 조회
  await fetchUserStats()

  // 2. 첫 번째 탭(게시글) 데이터 조회
  await fetchMyPosts()
})

</script>

<style scoped>
@import '@/assets/styles/components/my-page.css';
</style>

