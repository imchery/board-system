<template>
  <div class="post-create-container">
    <div class="post-create">
      <el-card class="create-card" v-loading="loading" element-loading-text="게시글을 불러오는 중...">

        <!-- 헤더 -->
        <div class="create-header">
          <h1 class="create-title">게시글 수정</h1>
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

        <!-- 수정 폼 -->
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
              취소
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
                :disabled="!canSubmit || !canEdit"
                :icon="EditPen"
            >
              {{ submitLoading ? '수정 중...' : '수정 완료' }}
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

        <div class="preview-body" v-html="postForm.content"></div>
      </div>

      <template #footer>
        <el-button @click="previewVisible = false">닫기</el-button>
        <el-button type="primary" @click="submitFromPreview">
          이대로 수정
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
import {useAuthStore} from '@/stores/auth'
import ClassicEditor from '@ckeditor/ckeditor5-build-classic'
import {Ckeditor} from "@ckeditor/ckeditor5-vue"
import {postApi} from "@/api/post.ts";
import {getTextLength} from "@/utils/textHelper.ts";
import {formatDate} from "@/utils/dateFormat.ts";
import {handlePostApiError} from "@/utils/errorHandler.ts";

// Props: URL 받아오는 게시글 ID=
interface Props {
  id: string
}

const props = defineProps<Props>()

// Router & Auth Store
const router = useRouter()
const authStore = useAuthStore()

// 반응형 데이터
const postFormRef = ref<FormInstance>()
const loading = ref(true)
const submitLoading = ref(false)
const previewVisible = ref(false)
const originalPost = ref<any>(null) // 원본 데이터 보관용

// CKEditor 설정
const editor = ClassicEditor as any
const editorConfig: any = {
  licenseKey: 'GPL',
  toolbar: [
    'heading', '|',
    'bold', 'italic', 'underline', '|',
    'fontSize', 'fontColor', 'fontBackgroundColor', '|',
    'alignment', '|',
    'bulletedList', 'numberedList', '|',
    'outdent', 'indent', '|',
    'link', 'insertTable', 'imageUpload', '|',
    'undo', 'redo',
  ],
  heading: {
    options: [
      {model: 'paragraph', title: '본문', class: 'ck-heading_paragraph'},
      {model: 'heading1', view: 'h1', title: '제목 1', class: 'ck-heading_heading1'},
      {model: 'heading2', view: 'h2', title: '제목 2', class: 'ck-heading_heading2'},
      {model: 'heading3', view: 'h3', title: '제목 3', class: 'ck-heading_heading3'}
    ]
  },
  fontSize: {
    options: [9, 11, 13, 'default', 17, 19, 21]
  },
}

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
      getTextLength(postForm.content) >= 10 &&
      postForm.category.length > 0
})

const canPreview = computed(() => {
  return postForm.title.trim().length > 0 || postForm.content.trim().length > 0
})

// 수정 권한 확인
const canEdit = computed(() => {
  if (!originalPost.value) return false
  return authStore.currentUser === originalPost.value.author
})

// 변경 사항 감지
const hasChanges = computed(() => {
  if (!originalPost.value) return false

  return originalPost.value.title !== postForm.title.trim() ||
      originalPost.value.content !== postForm.content.trim() ||
      originalPost.value.category === postForm.category
})

// 미리보기 표시
const showPreview = () => {
  previewVisible.value = true
}

// 미리보기에서 수정
const submitFromPreview = () => {
  previewVisible.value = false
  handleSubmit()
}

// 기존 게시글 데이터 로드
const fetchPost = async () => {
  try {
    loading.value = true
    console.log('기존 게시글 데이터 로드:', props.id)

    const response = await postApi.getPost(props.id)

    if (response.result && response.data) {

      // 원본 데이터 보관(권한 체크용)
      originalPost.value = response.data

      // 권한 체크
      if (!canEdit.value) {
        if (!authStore.isLoggedIn) {
          ElMessage.error('로그인이 필요합니다')
          await router.push('/login')
        } else {
          ElMessage.error('수정 권한이 없습니다')
          await router.push(`/posts/${props.id}`)
        }
        return false
      }

      postForm.title = response.data.title
      postForm.content = response.data.content
      postForm.category = response.data.category || ''

      console.log('게시글 로드 완료:', response.data)
    } else {
      ElMessage.error(response.message || '게시글을 불러오는데 실패했습니다')
      // 실패 시 목록으로 이동
      await router.push('/posts')
    }
  } catch (error: any) {
    handlePostApiError(error, 'detail')
  } finally {
    loading.value = false
  }
}

// 게시글 수정
const handleSubmit = async () => {
  if (!postFormRef.value) return

  const isValid = await postFormRef.value.validate().catch(() => false)
  if (!isValid) return

  if (!canEdit.value) {
    ElMessage.error('수정 권한이 없습니다')
    return
  }

  try {
    submitLoading.value = true

    const response = await postApi.updatePost(props.id, {
      title: postForm.title.trim(),
      content: postForm.content.trim(),
      category: postForm.category,
    })

    if (response.result) {
      ElMessage.success('게시글이 수정되었습니다')

      await router.push(`/posts/${props.id}`)
    } else {
      ElMessage.error(response.message || '게시글 수정에 실패했습니다')
    }
  } catch (error: any) {
    handlePostApiError(error, 'edit')
  } finally {
    submitLoading.value = false
  }
}

// 폼 초기화
const resetForm = async () => {
  if (!originalPost.value) return

  try {
    await ElMessageBox.confirm(
        '작성 중인 내용이 모두 초기화됩니다. 계속하시겠습니까?',
        '폼 초기화',
        {
          confirmButtonText: '초기화',
          cancelButtonText: '취소',
          type: 'warning'
        }
    )

    // 원본 데이터로 되돌리기
    postForm.title = originalPost.value.title
    postForm.content = originalPost.value.content
    postForm.category = originalPost.value.category || ''

    // 폼 검증 상태도 초기화
    postFormRef.value?.clearValidate()

    ElMessage.success('폼이 초기화되었습니다')
  } catch (error) {
    return
  }
}

// 상세화면으로 돌아가기
const goBack = async () => {
  // 변경 사항이 있으면 확인 다이얼로그
  if (hasChanges.value) {
    try {
      await ElMessageBox.confirm(
          '저장하지 않은 변경사항이 있습니다. 정말 나가시겠습니까?',
          '페이지 나가기',
          {
            confirmButtonText: '나가기',
            cancelButtonText: '계속 수정',
            type: 'warning'
          }
      )
    } catch (error) {
      return
    }
  }
  await router.push(`/posts/${props.id}`)
}

// 페이지 이탈 방지
const handleBeforeUnload = (e: BeforeUnloadEvent) => {
  if (hasChanges.value) {
    e.preventDefault()
    return ''
  }
}

// 컴포넌트 라이프사이클
onMounted(async () => {
  window.addEventListener('beforeunload', handleBeforeUnload)
  await fetchPost()
})

onBeforeUnmount(() => {
  window.removeEventListener('beforeunload', handleBeforeUnload)
})
</script>

<style scoped>
@import '@/assets/styles/components/post-create.css';
</style>
