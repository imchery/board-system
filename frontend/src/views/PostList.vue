<template>
  <div class="post-list page-container">
    <!-- 헤더 영역 -->
    <el-card class="header-card">
      <div class="header-content">
        <div class="header-title">
          <h2>게시글 목록</h2>
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
        <el-table-column prop="id" label="번호" width="120"/>
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
        <el-table-column prop="viewCount" label="조회수" width="120"/>
        <el-table-column prop="likeCount" label="좋아요" width="120">
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
@import url('https://fonts.googleapis.com/css2?family=Pretendard:wght@300;400;500;600;700;800&display=swap');

.post-list {
  padding: 20px;
  min-height: calc(100vh - 70px);
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 50%, #f1f5f9 100%);
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

/* 카드 공통 스타일 */
.header-card,
.search-card,
.table-card {
  margin-bottom: 16px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(226, 232, 240, 0.5);
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
}

.header-card:hover,
.search-card:hover,
.table-card:hover {
  background: rgba(255, 255, 255, 0.95);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
}

/* 헤더 카드 */
.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding: 16px 20px;
}

.header-title h2 {
  margin: 0;
  font-size: 25px;
  font-weight: 700;
  color: #1e293b;
  font-family: 'Pretendard', sans-serif;
  letter-spacing: -0.02em;
}

/* 버튼 스타일 통일 */
.header-actions .el-button,
.search-card .el-button {
  background: linear-gradient(135deg, #6366f1, #4f46e5);
  border: none;
  color: white;
  border-radius: 10px;
  padding: 10px 20px;
  font-weight: 600;
  font-size: 14px;
  font-family: 'Pretendard', sans-serif;
  letter-spacing: -0.01em;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 14px rgba(99, 102, 241, 0.25);
  height: 40px;
  position: relative;
  overflow: hidden;
}

.header-actions .el-button::before,
.search-card .el-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

.header-actions .el-button:hover::before,
.search-card .el-button:hover::before {
  left: 100%;
}

.header-actions .el-button:hover,
.search-card .el-button:hover {
  background: linear-gradient(135deg, #5b21b6, #4c1d95);
  transform: translateY(-2px) scale(1.02);
  box-shadow: 0 8px 20px rgba(99, 102, 241, 0.35);
}

.header-actions .el-button:active,
.search-card .el-button:active {
  transform: translateY(0) scale(0.98);
  transition: all 0.1s ease;
}

/* 검색 카드 */
.search-card {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95), rgba(255, 255, 255, 0.85));
  border: 1px solid rgba(99, 102, 241, 0.1);
}

.search-card .el-row {
  padding: 20px;
}

.search-card .el-col:first-child {
  padding-right: 12px;
}

.search-card .el-col:last-child {
  padding-left: 12px;
}

:deep(.search-card .el-input__wrapper) {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9), rgba(248, 250, 252, 0.9));
  border: 2px solid rgba(99, 102, 241, 0.1);
  border-radius: 12px;
  box-shadow:
      0 4px 6px rgba(0, 0, 0, 0.02),
      inset 0 1px 0 rgba(255, 255, 255, 0.5);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  height: 40px;
  position: relative;
  overflow: hidden;
}

:deep(.search-card .el-input__wrapper::before) {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(99, 102, 241, 0.3), transparent);
  opacity: 0;
  transition: opacity 0.3s ease;
}

:deep(.search-card .el-input__wrapper:hover) {
  background: linear-gradient(135deg, rgba(255, 255, 255, 1), rgba(248, 250, 252, 1));
  border-color: rgba(99, 102, 241, 0.3);
  box-shadow:
      0 8px 15px rgba(0, 0, 0, 0.04),
      inset 0 1px 0 rgba(255, 255, 255, 0.7);
  transform: translateY(-1px);
}

:deep(.search-card .el-input__wrapper:hover::before) {
  opacity: 1;
}

:deep(.search-card .el-input__wrapper.is-focus) {
  background: linear-gradient(135deg, rgba(255, 255, 255, 1), rgba(248, 250, 252, 1));
  border-color: #6366f1;
  box-shadow:
      0 0 0 4px rgba(99, 102, 241, 0.1),
      0 8px 20px rgba(99, 102, 241, 0.1),
      inset 0 1px 0 rgba(255, 255, 255, 0.7);
  transform: translateY(-1px);
}

:deep(.search-card .el-input__wrapper.is-focus::before) {
  opacity: 1;
  background: linear-gradient(90deg, transparent, rgba(99, 102, 241, 0.5), transparent);
}

:deep(.search-card .el-input__inner) {
  color: #374151;
  font-weight: 500;
  font-family: 'Pretendard', sans-serif;
  height: 36px;
  line-height: 36px;
  font-size: 14px;
  letter-spacing: -0.01em;
}

:deep(.search-card .el-input__inner::placeholder) {
  color: #9ca3af;
  font-family: 'Pretendard', sans-serif;
  font-weight: 400;
}

:deep(.search-card .el-input__prefix) {
  color: #6366f1;
  transition: all 0.3s ease;
}

:deep(.search-card .el-input__wrapper:hover .el-input__prefix),
:deep(.search-card .el-input__wrapper.is-focus .el-input__prefix) {
  color: #5b21b6;
  transform: scale(1.1);
}

/* 테이블 카드 */
.table-card {
  padding: 20px;
}

/* 테이블 스타일 */
:deep(.el-table) {
  background: transparent;
  border-radius: 8px;
  overflow: hidden;
  font-family: 'Pretendard', sans-serif;
}

:deep(.el-table__header-wrapper) {
  background: rgba(248, 250, 252, 0.8);
}

:deep(.el-table__header th) {
  background: transparent;
  border-bottom: 2px solid #e2e8f0;
  color: #374151;
  font-weight: 700;
  font-size: 14px;
  font-family: 'Pretendard', sans-serif;
  letter-spacing: -0.01em;
  padding: 12px;
  height: 44px;
}

:deep(.el-table__body-wrapper) {
  background: rgba(255, 255, 255, 0.5);
}

:deep(.el-table tbody tr) {
  background: transparent;
  border-bottom: 1px solid #f1f5f9;
  cursor: pointer;
  transition: all 0.2s ease;
  height: 48px;
}

:deep(.el-table tbody tr:hover) {
  background: rgba(99, 102, 241, 0.04) !important;
  transform: translateX(2px);
}

:deep(.el-table td) {
  border-bottom: 1px solid #f1f5f9;
  color: #374151;
  font-weight: 500;
  font-family: 'Pretendard', sans-serif;
  letter-spacing: -0.01em;
  padding: 12px;
}

.post-title {
  color: #374151;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 600;
  font-family: 'Pretendard', sans-serif;
  letter-spacing: -0.02em;
}

.post-title:hover {
  color: #6366f1;
  transform: translateX(3px);
}

.comment-count {
  color: #6b7280;
  font-size: 12px;
  margin-left: 8px;
  background: linear-gradient(135deg, #f3f4f6, #e5e7eb);
  padding: 3px 8px;
  border-radius: 8px;
  font-weight: 600;
  font-family: 'Pretendard', sans-serif;
  letter-spacing: -0.01em;
}

/* 페이지네이션 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

:deep(.el-pagination) {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  padding: 12px 20px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  font-family: 'Pretendard', sans-serif;
}

:deep(.el-pagination .el-pager li) {
  background: transparent;
  border-radius: 6px;
  color: #6b7280;
  font-weight: 600;
  font-family: 'Pretendard', sans-serif;
  margin: 0 2px;
  transition: all 0.2s ease;
  height: 32px;
  line-height: 32px;
  min-width: 32px;
}

:deep(.el-pagination .el-pager li:hover) {
  background: rgba(99, 102, 241, 0.1);
  color: #6366f1;
}

:deep(.el-pagination .el-pager li.is-active) {
  background: linear-gradient(135deg, #6366f1, #4f46e5);
  color: white;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.3);
}

:deep(.el-pagination .btn-prev),
:deep(.el-pagination .btn-next) {
  background: transparent;
  border-radius: 6px;
  color: #6b7280;
  font-weight: 600;
  font-family: 'Pretendard', sans-serif;
  transition: all 0.2s ease;
  height: 32px;
  line-height: 32px;
}

:deep(.el-pagination .btn-prev:hover),
:deep(.el-pagination .btn-next:hover) {
  background: rgba(99, 102, 241, 0.1);
  color: #6366f1;
}

:deep(.el-pagination .el-select) {
  margin: 0 8px;
}

:deep(.el-pagination .el-select .el-select__wrapper) {
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  height: 32px;
}

:deep(.el-pagination .el-select .el-select__selected-item) {
  color: #374151;
  font-weight: 500;
  font-family: 'Pretendard', sans-serif;
  font-size: 14px;
}

:deep(.el-pagination .el-pagination__total),
:deep(.el-pagination .el-pagination__jump) {
  color: #6b7280;
  font-weight: 500;
  font-family: 'Pretendard', sans-serif;
  font-size: 14px;
  margin: 0 8px;
}

/* 좋아요 아이콘 색상 */
:deep(.el-table .el-icon) {
  color: #ef4444;
}

/* 로딩 스타일 */
:deep(.el-loading-mask) {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(5px);
}

:deep(.el-loading-spinner) {
  color: #6366f1;
}

/* 반응형 디자인 */
@media (max-width: 768px) {
  .post-list {
    padding: 16px;
  }

  .header-content {
    flex-direction: column;
    gap: 12px;
    padding: 16px;
  }

  .header-title h2 {
    font-size: 18px;
  }

  .table-card {
    padding: 16px;
  }

  .search-card .el-row {
    padding: 16px;
  }

  .search-card .el-col {
    margin-bottom: 8px;
  }

  .search-card .el-col:first-child {
    padding-right: 0;
  }

  .search-card .el-col:last-child {
    padding-left: 0;
  }

  :deep(.el-table) {
    font-size: 14px;
  }

  :deep(.el-table td),
  :deep(.el-table th) {
    padding: 8px;
  }
}

@media (max-width: 480px) {
  .header-content {
    padding: 12px;
  }

  .table-card {
    padding: 12px;
  }

  .header-title h2 {
    font-size: 16px;
  }

  .search-card .el-row {
    padding: 12px;
  }
}
</style>
