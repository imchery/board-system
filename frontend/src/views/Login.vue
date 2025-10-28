<template>
  <div class="login-container">
    <el-container>
      <el-main class="login-main">
        <div class="login-box">
          <!-- 로고 영역 -->
          <div class="logo-section">
            <h2>Board System</h2>
            <p>로그인하여 게시판을 이용해보세요</p>
          </div>

          <!--
            ** 로그인 폼 **
            ref: JavaScript 연결용
            :model: 데이터 바인딩
            :rules: 검증규칙
          -->
          <el-form
              ref="loginFormRef"
              :model="loginForm"
              :rules="loginRules"
              class="login-form"
              @submit.prevent="handleLogin"
          >

            <!--
              ** 사용자명 입력 **
              prop: 폼 아이템에서 prop 지정
              v-model: 양방향 데이터 바인딩
            -->
            <el-form-item prop="username">
              <el-input
                  v-model="loginForm.username"
                  placeholder="사용자명"
                  size="large"
                  :prefix-icon="User"
                  @keyup.enter="handleLogin"
              />
            </el-form-item>

            <!-- 비밀번호 입력 -->
            <el-form-item prop="password">
              <el-input
                  v-model="loginForm.password"
                  type="password"
                  placeholder="비밀번호"
                  size="large"
                  :prefix-icon="Lock"
                  show-password
                  @keyup.enter="handleLogin"
              />
            </el-form-item>

            <!-- 로그인 버튼 -->
            <el-form-item>
              <el-button
                  type="primary"
                  size="large"
                  :loading="authStore.isLoading"
                  @click="handleLogin"
                  class="login-button"
              >
                {{ authStore.isLoading ? '로그인 중...' : '로그인' }}
              </el-button>
            </el-form-item>
          </el-form>

          <!-- 회원가입 링크 추가 -->
          <div class="signup-link">
            <span>아직 계정이 없으신가요?</span>
            <el-button type="text" @click="goToSignup">회원가입</el-button>
          </div>

          <!-- 기본 계정 안내 -->
          <div class="demo-info">
            <el-alert
                title="데모 계정"
                type="info"
                :closable="false"
                show-icon
            >
              <template #default>
                <p><strong>사용자명:</strong> admin</p>
                <p><strong>비밀번호:</strong> admin123</p>
              </template>
            </el-alert>
          </div>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import {handleAuthApiError} from "@/utils/errorHandler.ts";
import {LoginRequest} from "@/types/api.ts";

// Vue Router & Auth Store
const router = useRouter()
const authStore = useAuthStore()

// 반응형 데이터
const loginFormRef = ref<FormInstance>()

// 로그인 폼 데이터
const loginForm = reactive({
  username: '',
  password: ''
})

// 폼 검증 규칙
const loginRules: FormRules = {
  username: [
    { required: true, message: '사용자명을 입력해주세요', trigger: 'blur' },
    { min: 2, max: 20, message: '사용자명은 2-20자 사이여야 합니다', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '비밀번호를 입력해주세요', trigger: 'blur' },
    { min: 6, max: 20, message: '비밀번호는 6-20자 사이여야 합니다', trigger: 'blur' }
  ]
}

// 로그인 처리 함수
const handleLogin = async () => {
  // 폼 검증
  if (!loginFormRef.value) return

  const isValid = await loginFormRef.value.validate().catch(() => false)
  if (!isValid) return

  try {
    const request: LoginRequest = {
      username: loginForm.username,
      password: loginForm.password
    }

    // 스토어를 통한 로그인
    const result = await authStore.login(request)

    if (result.success) {
      ElMessage.success(result.message || '로그인 성공!')

      // 게시글 목록으로 이동
      await router.push('/posts')
    } else {
      ElMessage.error(result.message || '로그인에 실패했습니다')
    }

  } catch (error) {
    handleAuthApiError(error)
  }
}

// 회원가입 페이지로 이동
const goToSignup = () => {
  router.push('/signup')
}

// 컴포넌트가 마운트될 때 이미 로그인된 상태면 리다이렉트
onMounted(() => {
  if (authStore.isLoggedIn) {
    router.push('/posts')
  }
})
</script>

<style scoped>
@import "@/assets/styles/components/login.css";
</style>
