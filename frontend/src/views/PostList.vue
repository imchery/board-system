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
          empty-text="게시글이 없습니다"
      >
        <!--    연번 컬럼(ID 대신)    -->
        <el-table-column prop="id" label="번호" width="80" align="center">
          <template #default="{$index}">
            <span class="row-number">{{ getRowNumber($index) }}</span>
          </template>
        </el-table-column>

        <!--    제목 컬럼    -->
        <el-table-column prop="title" label="제목" min-width="300">
          <template #default="scope">
            <div class="post-title">
              {{ scope.row.title }}
              <span v-if="scope.row.commentCount > 0" class="comment-count">
                [{{ scope.row.commentCount }}]
              </span>
            </div>
          </template>
        </el-table-column>

        <!--    작성자 컬럼    -->
        <el-table-column prop="author" label="작성자" width="120" align="center">
          <template #default="scope">
            <span class="author-name">
              {{ typeof scope.row.author === 'string' ? scope.row.author : scope.row.author.username }}
            </span>
          </template>
        </el-table-column>

        <!--    조회수 컬럼    -->
        <el-table-column prop="viewCount" label="조회" width="80" align="center">
          <template #default="scope">
            <span class="view-count">{{ scope.row.viewCount || 0 }}</span>
          </template>
        </el-table-column>

        <!--    좋아요 컬럼    -->
        <el-table-column prop="likeCount" label="좋아요" width="100" align="center">
          <template #default="scope">
            <span class="like-count">
              <el-icon class="heart-icon">
                <svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
                  <path fill="currentColor" d="M923 283.6c-13.4-31.1-32.6-58.9-56.9-82.8-24.3-23.8-52.5-42.4-84-55.5-32.5-13.5-66.9-20.3-102.4-20.3-49.3 0-97.4 13.5-139.2 39-10 6.1-19.5 12.8-28.5 20.1-9-7.3-18.5-14-28.5-20.1-41.8-25.5-89.9-39-139.2-39-35.5 0-69.9 6.8-102.4 20.3-31.4 13-59.7 31.7-84 55.5-24.4 23.9-43.5 51.7-56.9 82.8-13.9 32.3-21 66.6-21 101.9 0 33.3 6.8 68 20.3 103.3 11.3 29.5 27.5 60.1 48.2 91 32.8 48.9 77.9 99.9 133.9 151.6 92.8 85.7 184.7 144.9 188.6 147.3l23.7 15.2c10.5 6.7 24 6.7 34.5 0l23.7-15.2c3.9-2.5 95.7-61.6 188.6-147.3 56-51.7 101.1-102.7 133.9-151.6 20.7-30.9 37-61.5 48.2-91 13.5-35.3 20.3-70 20.3-103.3.1-35.3-7-69.6-20.9-101.9z"/>
                </svg>
              </el-icon>
              {{ scope.row.likeCount || 0 }}
            </span>
          </template>
        </el-table-column>

        <!--    작성일 컬럼    -->
        <el-table-column prop="createdAt" label="작성일" width="120" align="center">
          <template #default="scope">
            <span class="post-date">{{ formatDate(scope.row.createdAt) }}</span>
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
import {Edit, Promotion, Search} from "@element-plus/icons-vue";

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

// 연번 계산 함수
const getRowNumber = (index: number) => {
  // 전체 게시글 수에서 현재 위치를 빼서 역순 번호 계산
  // 예: 총 50개, 1페이지(10개), 첫 번째 = 50 - 0 = 50
  // 예: 총 50개, 2페이지(10개), 첫 번째 = 50 - 10 - 0 = 40
  return totalElements.value - (currentPage.value - 1) * pageSize.value - index
}
</script>

<style scoped>
/* 외부 CSS 파일 import */
@import '@/assets/styles/components/post-list.css';

/* 폰트 */
@import url('https://fonts.googleapis.com/css2?family=Pretendard:wght@300;400;500;600;700;800&display=swap');
</style>
