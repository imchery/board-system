<template>
  <div class="comment-list">
    <!--  댓글 작성 폼 (로그인한 경우만)  -->
    <div v-if="authStore.isLoggedIn" class="comment-form-actions">
      <CommentForm :post-id="props.postId" @succes="handleCommentSuccess" parent-comment-id=""/>
    </div>

    <!--  로그인 안내 (비로그인 사용자)  -->
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

    <!--  댓글 목록 헤더  -->
    <div class="comments-header">
      <h3>댓글 {{ totalComments }}개</h3>
      <div class="sort-options">
        <el-radio-group v-model="sortBy" size="small" @change="handleSortChange">
          <el-radio-button value="latest">최신순</el-radio-button>
          <el-radio-button value="oldest">오래된순</el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <!--  로딩 상태  -->
    <div v-if="loading" class="comments-loading">
      <el-skeleton animated>
        <template #template>
          <div v-for="n in 3" :key="n" class="comment-skeleton">
            <div style="display: flex; align-items: center; margin-bottom: 12px;">
              <el-skeleton-item variant="circle" style="width: 32px; height: 32px; margin-right: 12px;"/>
              <div style="flex: 1">
                <el-skeleton-item variant="text" style="width: 30%; margin-bottom: 8px;"/>
                <el-skeleton-item variant="text" style="width: 20%;"/>
              </div>
            </div>
            <el-skeleton-item variant="text" style="width: 80%; margin-bottom: 8px;"/>
            <el-skeleton-item variant="text" style="width: 60%;"/>
          </div>
        </template>
      </el-skeleton>
    </div>

    <!--  댓글 목록  -->
    <div v-else-if="comments.length > 0" class="comments-content">
      <CommentItem
          v-for="comment in comments"
          :key="comment.id"
          :comment="comment"
          @updated="refreshComments"
          @deleted="refreshComments"
      />

      <!--   페이징   -->
      <div v-if="totalPages > 1" class="comments-pagination">
        <el-pagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="totalComments"
            layout="prev, pager, next"
            @current-change="handlePageChange"
            small
        />
      </div>
    </div>
    <!--  댓글 없음  -->
    <div v-else class="no-comments">
      <el-empty description="첫 번째 댓글을 작성해보세요!"/>
    </div>
  </div>
</template>

<script setup lang="ts">
// Props
import {useAuthStore} from "@/stores/auth.ts";
import {useRouter} from "vue-router";
import {CommentResponse} from "@/types/api.ts";
import {onMounted, ref} from 'vue'
import CommentForm from "@/components/comment/CommentForm.vue";
import CommentItem from "@/components/comment/CommentItem.vue";
import {commentApi} from "@/api/comment.ts";
import {ElMessage} from "element-plus";
import {handleCommentApiError} from "@/utils/errorHandler.ts";

interface Props {
  postId: string
}

const props = defineProps<Props>()

// 스토어 & 라우터
const authStore = useAuthStore()
const router = useRouter()

// 반응형 데이터
const comments = ref<CommentResponse[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const totalComments = ref(0)
const totalPages = ref(0)
const sortBy = ref('latest')

// 댓글 목록 조회
const loadComments = async () => {
  try {
    loading.value = true

    const response = await commentApi.getRootComments(
        props.postId,
        currentPage.value - 1, // 백엔드는 0부터 시작
        pageSize.value
    )

    if (response.result) {
      comments.value = response.data.content
      totalComments.value = response.data.totalElements // 전체 요소 개수
      totalPages.value = response.data.totalPages // 전체 페이지 수
    } else {
      ElMessage.error(response.message || '댓글을 불러오는데 실패했습니다')
    }
  } catch (error) {
    handleCommentApiError(error, 'load')
  } finally {
    loading.value = false
  }
}

// 댓글 작성 성공 처리
const handleCommentSuccess = () => {
  // 첫 페이지로 이동하여 새 댓글 확인
  currentPage.value = 1
  loadComments()
}

// 로그인 페이지로 이동
const goToLogin = () => {
  router.push("/login")
}

// 정렬 변경
const handleSortChange = () => {
  // TODO: 백엔드에서 정렬 지원 시 구현
  // 현재는 최신순만 지원
  loadComments()
}

// 댓글 새로고침
const refreshComments = () => {
  loadComments()
}

// 페이지 변경
const handlePageChange = (page: number) => {
  currentPage.value = page
  loadComments()
}

// 컴포넌트 마운트 시 댓글 로드
onMounted(() => {
  loadComments()
})

// 외부에서 호출할 수 있도록 expose
defineExpose({
  refreshComments,
  loadComments
})

</script>

<style scoped>
@import '@/assets/styles/components/comment.css';
</style>
