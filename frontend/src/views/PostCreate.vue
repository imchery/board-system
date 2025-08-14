<template>
  <div class="post-create-container">
    <div class="post-create">
      <el-card class="create-card">

        <!-- 헤더 -->
        <div class="create-header">
          <h1 class="create-title">게시글 작성</h1>
          <div class="header-actions">
            <el-button @click="showPreview" :disabled="!canPreview" text>
              <el-icon>
                <View/>
              </el-icon>
              미리보기
            </el-button>
          </div>
        </div>

        <el-divider/>

        <!-- 작성 폼 -->
        <el-form
            ref="postFormRef"
            :model="postForm"
            :rules="postRules"
            label-position="top"
            class="post-form"
        >
          <!-- 카테고리 선택 -->
          <el-form-item label="카테고리" prop="category">
            <el-select
                v-model="postForm.category"
                placeholder="카테고리를 선택하세요"
                class="category-select"
            >
              <el-option label="자유게시판" value="자유게시판"/>
              <el-option label="질문게시판" value="질문게시판"/>
              <el-option label="정보공유" value="정보공유"/>
              <el-option label="공지사항" value="공지사항"/>
            </el-select>
          </el-form-item>

          <!-- 제목 입력 -->
          <el-form-item label="제목" prop="title">
            <el-input
                v-model="postForm.title"
                placeholder="제목을 입력하세요 (최대 100자)"
                maxlength="100"
                show-word-limit
                class="title-input"
            />
          </el-form-item>

          <!-- 내용 입력 부분 -->
          <el-form-item label="내용" prop="content">
            <div class="editor-container">
              <div class="editor-wrapper">
                <Ckeditor
                    :editor="editor"
                    v-model="postForm.content"
                    :config="editorConfig"
                    class="large-editor"
                />
              </div>
              <div class="editor-footer">
                <div class="content-counter">
                  {{ getTextLength(postForm.content) }} / 10,000 글자
                </div>
              </div>
            </div>
          </el-form-item>
        </el-form>

        <el-divider/>

        <!-- 하단 액션 -->
        <div class="create-actions">
          <div class="action-left">
            <el-button @click="goBack" :icon="ArrowLeft">
              목록으로
            </el-button>
          </div>

          <div class="action-right">
            <el-button @click="resetForm" :icon="RefreshRight">
              초기화
            </el-button>
            <el-button
                type="primary"
                @click="handleSubmit"
                :loading="submitLoading"
                :disabled="!canSubmit"
                :icon="EditPen"
            >
              {{ submitLoading ? '등록 중...' : '게시글 등록' }}
            </el-button>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 미리보기 다이얼로그 -->
    <el-dialog
        v-model="previewVisible"
        title="게시글 미리보기"
        width="80%"
        class="preview-dialog"
    >
      <div class="preview-content">
        <div class="preview-meta">
          <el-tag type="primary">{{ postForm.category || '미분류' }}</el-tag>
          <span class="preview-author">{{ authStore.currentUser }}</span>
          <span class="preview-date">{{ formatDate(new Date()) }}</span>
        </div>

        <h1 class="preview-title">{{ postForm.title || '제목 없음' }}</h1>

        <div class="preview-body" v-html="formatContent(postForm.content)"></div>
      </div>

      <template #footer>
        <el-button @click="previewVisible = false">닫기</el-button>
        <el-button type="primary" @click="submitFromPreview">
          이대로 등록
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import {computed, onBeforeUnmount, onMounted, reactive, ref} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage, ElMessageBox, type FormInstance, type FormRules} from 'element-plus'
import {ArrowLeft, EditPen, RefreshRight, View} from '@element-plus/icons-vue'
import {postApi} from '@/api/post'
import {useAuthStore} from '@/stores/auth'
import ClassicEditor from '@ckeditor/ckeditor5-build-classic'
import {Ckeditor} from "@ckeditor/ckeditor5-vue";

// CKEditor 설정
const editor = ClassicEditor
const editorConfig = {
  licenseKey: 'GPL',
  toolbar: [
    'heading', '|',
    'bold', 'italic', 'underline', '|',
    'fontSize', 'fontColor', 'fontBackgroundColor', '|',
    'alignment', '|',
    'bulletedList', 'numberedList', '|',
    'outdent', 'indent', '|',
    'link', 'insertTable', '|',
    'undo', 'redo'
  ],
  heading: {
    options: [
      { model: 'paragraph', title: '본문', class: 'ck-heading_paragraph' },
      { model: 'heading1', view: 'h1', title: '제목 1', class: 'ck-heading_heading1' },
      { model: 'heading2', view: 'h2', title: '제목 2', class: 'ck-heading_heading2' },
      { model: 'heading3', view: 'h3', title: '제목 3', class: 'ck-heading_heading3' }
    ]
  },
  fontSize: {
    options: [ 9, 11, 13, 'default', 17, 19, 21 ]
  }
}

// HTML에서 텍스트 길이 추출
const getTextLength = (htmlContent: string) => {
  if (!htmlContent) return 0
  const div = document.createElement('div')
  div.innerHTML = htmlContent
  return div.textContent?.length || 0
}

// Router & Auth Store
const router = useRouter()
const authStore = useAuthStore()

// 반응형 데이터
const postFormRef = ref<FormInstance>()
const submitLoading = ref(false)
const previewVisible = ref(false)

// 게시글 폼 데이터
const postForm = reactive({
  title: '',
  content: '',
  category: ''
})

// 폼 검증 규칙
const postRules: FormRules = {
  category: [
    {required: true, message: '카테고리를 선택해주세요', trigger: 'change'}
  ],
  title: [
    {required: true, message: '제목을 입력해주세요', trigger: 'blur'},
    {min: 2, max: 100, message: '제목은 2-100자 사이여야 합니다', trigger: 'blur'}
  ],
  content: [
    {required: true, message: '내용을 입력해주세요', trigger: 'blur'},
    {min: 10, max: 10000, message: '내용은 10-10000자 사이여야 합니다', trigger: 'blur'}
  ]
}

// 계산된 속성
const canSubmit = computed(() => {
  return postForm.title.trim().length >= 2 &&
      postForm.content.trim().length >= 10 &&
      postForm.category.length > 0
})

const canPreview = computed(() => {
  return postForm.title.trim().length > 0 || postForm.content.trim().length > 0
})

// 미리보기 표시
const showPreview = () => {
  previewVisible.value = true
}

// 미리보기에서 등록
const submitFromPreview = () => {
  previewVisible.value = false
  handleSubmit()
}

// 폼 초기화
const resetForm = async () => {
  try {
    await ElMessageBox.confirm(
        '작성 중인 내용이 모두 삭제됩니다. 계속하시겠습니까?',
        '폼 초기화',
        {
          confirmButtonText: '초기화',
          cancelButtonText: '취소',
          type: 'warning'
        }
    )

    postFormRef.value?.resetFields()
    postForm.title = ''
    postForm.content = ''
    postForm.category = ''
    ElMessage.success('폼이 초기화되었습니다')
  } catch (error) {
    // 사용자가 취소한 경우
  }
}

// 게시글 등록
const handleSubmit = async () => {
  if (!postFormRef.value) return

  // 폼 검증
  const isValid = await postFormRef.value.validate().catch(() => false)
  if (!isValid) return

  try {
    submitLoading.value = true

    const response = await postApi.createPost({
      title: postForm.title.trim(),
      content: postForm.content.trim(),
      category: postForm.category
    })

    if (response.result) {
      ElMessage.success('게시글이 등록되었습니다')

      // 생성된 게시글 상세로 이동
      router.push(`/posts/${response.data.id}`)
    } else {
      ElMessage.error(response.message || '게시글 등록에 실패했습니다')
    }
  } catch (error) {
    console.error('게시글 등록 실패:', error)
    ElMessage.error('게시글 등록 중 오류가 발생했습니다')
  } finally {
    submitLoading.value = false
  }
}

// 목록으로 돌아가기
const goBack = async () => {
  // 작성 중인 내용이 있으면 확인
  if (postForm.title.trim() || postForm.content.trim()) {
    try {
      await ElMessageBox.confirm(
          '작성 중인 내용이 있습니다. 정말 나가시겠습니까?',
          '페이지 나가기',
          {
            confirmButtonText: '나가기',
            cancelButtonText: '계속 작성',
            type: 'warning'
          }
      )
    } catch (error) {
      return // 사용자가 취소한 경우
    }
  }

  router.push('/posts')
}

// 내용 포맷팅 (간단한 마크다운)
const formatContent = (content: string) => {
  if (!content) return '내용이 없습니다'

  return content
      .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
      .replace(/\*(.*?)\*/g, '<em>$1</em>')
      .replace(/`(.*?)`/g, '<code>$1</code>')
      .replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank">$1</a>')
      .replace(/\n/g, '<br>')
}

// 날짜 포맷팅
const formatDate = (date: Date) => {
  return date.toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 페이지 이탈 방지
const handleBeforeUnload = (e: BeforeUnloadEvent) => {
  if (postForm.title.trim() || postForm.content.trim()) {
    e.preventDefault()
    return ''
  }
}

// 컴포넌트 라이프사이클
onMounted(() => {
  window.addEventListener('beforeunload', handleBeforeUnload)
})

onBeforeUnmount(() => {
  window.removeEventListener('beforeunload', handleBeforeUnload)
})
</script>

<style scoped>
@import '@/assets/styles/components/post-create.css';
</style>
