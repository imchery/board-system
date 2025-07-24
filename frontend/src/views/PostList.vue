<template>
  <div class="post-list">
    <!-- 헤더 영역 -->
    <el-card class="header-card">
      <div class="header-content">
        <div class="header-title">
          <h2>📝 게시글 목록</h2>
        </div>
        <div class="header-actions">
          <el-button type="primary" @click="goToCreate">
            <el-icon>
              <Edit/>
            </el-icon>
            글쓰기
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 검색 영역 -->
    <el-card class="search-card">
      <el-row :gutter="20">
        <el-col :span="18">
          <el-input
              v-model="searchKeyword"
              placeholder="제목이나 내용을 검색하세요"
              @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon>
                <Search/>
              </el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="6">
          <el-button type="primary" @click="handleSearch">
            검색
          </el-button>
          <el-button @click="resetSearch">
            초기화
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 게시글 테이블 -->
    <el-card class="table-card">
      <el-table
          :data="posts"
          v-loading="loading"
          @row-click="goToDetail"
          style="width: 100%"
      >
        <el-table-column prop="id" label="번호" width="80"/>
        <el-table-column prop="title" label="제목" min-width="200">
          <template #default="{ row }">
            <span class="post-title">{{ row.title }}</span>
            <!-- 댓글 수 표시 (나중에 추가) -->
            <span class="comment-count" v-if="row.commentCount">
              [{{ row.commentCount }}]
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="author" label="작성자" width="120"/>
        <el-table-column prop="viewCount" label="조회수" width="80"/>
        <el-table-column prop="likeCount" label="좋아요" width="80">
          <template #default="{ row }">
            <span style="color: #f56c6c">
              <el-icon><Heart/></el-icon>
              {{ row.likeCount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="작성일" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
      </el-table>

      <!-- 페이징 -->
      <div class="pagination-wrapper">
        <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="totalElements"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import {onMounted, ref} from 'vue'
import {useRouter} from "vue-router"
import {ElMessage} from "element-plus";
import {postApi} from "@/api/post.ts";
import type {ApiError, PostResponse} from "@/types/api.ts";
import {Edit, Search} from "@element-plus/icons-vue";

// 반응형 데이터
const posts = ref<PostResponse[]>([])
const loading = ref<boolean>(false)
const searchKeyword = ref<string>('')

// 페이징 관련
const currentPage = ref<number>(1)
const pageSize = ref<number>(10)
const totalElements = ref<number>(0)

// Vue Router
const router = useRouter()

// 페이지 로드 시 게시글 목록 가져오기
onMounted(() => {
  fetchPosts()
})

// 게시글 목록 조회 함수
const fetchPosts = async () => {
  loading.value = true
  try {
    // 하나의 API 호출로 통일
    const response = await postApi.getPosts(
        currentPage.value - 1,
        pageSize.value,
        searchKeyword.value.trim()
    )

    console.log('API 응답:', response) // 디버깅용

    // 백엔드 ResponseVO 구조에 맞게 데이터 추출
    if (response && response.result) {
      posts.value = response.data.content || []
      totalElements.value = response.data.totalElements || 0
    } else {
      ElMessage.error(response.message || '게시글을 불러오는데 실패했습니다')
      posts.value = []
      totalElements.value = 0
    }
  } catch (error) {
    console.error('게시글 조회 실패:', error)

    const apiError = error as ApiError

    // 에러 타입별 처리
    if (apiError.response?.status === 401) {
      ElMessage.error('로그인이 필요합니다')
      router.push('/login')
    } else if (apiError.response?.status === 500) {
      ElMessage.error('서버 오류가 발생했습니다')
    } else {
      ElMessage.error('게시글을 불러오는데 실패했습니다')
    }

    // 에러 발생 시 빈 데이터로 설정
    posts.value = []
    totalElements.value = 0
  } finally {
    loading.value = false
  }
}

// 검색 처리
const handleSearch = () => {
  currentPage.value = 1
  fetchPosts()
}

// 검색 초기화
const resetSearch = () => {
  searchKeyword.value = ''
  currentPage.value = 1
  fetchPosts()
}

// 페이지 크기 변경
const handleSizeChange = (newSize: number) => {
  pageSize.value = newSize
  currentPage.value = 1
  fetchPosts()
}

// 페이지 번호 변경
const handleCurrentChange = (newPage: number) => {
  currentPage.value = newPage
  fetchPosts()
}

// 게시글 상세로 이동
const goToDetail = (row: any) => {
  router.push(`/posts/${row.id}`)
}

// 글쓰기 페이지로 이동
const goToCreate = () => {
  router.push('/posts/create')
}

// 날짜 포맷팅
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
    return date.toLocaleDateString('ko-KR')
  }
}
</script>

<style scoped>
.post-list {
  padding: 20px;
}

.header-card {
  margin-bottom: 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding: 0 8px;
}

.header-title h2 {
  margin: 0;
  font-size: 18px;
}

.header-actions .el-button {
  padding: 8px 16px;
  font-size: 14px;
}

.search-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.post-title {
  color: #303133;
  cursor: pointer;
}

.post-title:hover {
  color: #409eff;
  text-decoration: underline;
}

.comment-count {
  color: #909399;
  font-size: 12px;
  margin-left: 5px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

:deep(.el-table tbody tr) {
  cursor: pointer;
}

:deep(.el-table tbody tr:hover) {
  background-color: #f5f7fa;
}
</style>

