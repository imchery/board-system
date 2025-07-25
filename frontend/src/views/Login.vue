<template>
  <div class="login-container">
    <el-container>
      <el-main class="login-main">
        <div class="login-box">
          <!-- 로고 영역 -->
          <div class="logo-section">
            <h2>📝 Board System</h2>
            <p>로그인하여 게시판을 이용해보세요</p>
          </div>

          <!-- 로그인 폼 -->
          <el-form
              ref="loginFormRef"
              :model="loginForm"
              :rules="loginRules"
              class="login-form"
              @submit.prevent="handleLogin"
          >
            <!-- 사용자명 입력 -->
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
                  :loading="loading"
                  @click="handleLogin"
                  class="login-button"
              >
                {{ loading ? '로그인 중...' : '로그인' }}
              </el-button>
            </el-form-item>
          </el-form>

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
import {onMounted, reactive, ref} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage, type FormInstance, type FormRules} from 'element-plus'
import {Lock, User} from '@element-plus/icons-vue'
import {authApi} from '@/api/auth'
import type {ApiError} from '@/types/api'

// Vue Router
const router = useRouter()

// 반응형 데이터
const loading = ref<boolean>(false)
const loginFormRef = ref<FormInstance>()

// 로그인 폼 데이터
const loginForm = reactive({
  username: '',
  password: ''
})

// 폼 검증 규칙
const loginRules: FormRules = {
  username: [
    {required: true, message: '사용자명을 입력해주세요', trigger: 'blur'},
    {min: 2, max: 20, message: '사용자명은 2-20자 사이여야 합니다', trigger: 'blur'}
  ],
  password: [
    {required: true, message: '비밀번호를 입력해주세요', trigger: 'blur'},
    {min: 6, max: 20, message: '비밀번호는 6-20자 사이여야 합니다', trigger: 'blur'}
  ]
}

// 로그인 처리 함수
const handleLogin = async () => {
  // 폼 검증
  if (!loginFormRef.value) return

  const isValid = await loginFormRef.value.validate().catch(() => false)
  if (!isValid) return

  loading.value = true

  try {
    // 로그인 API 호출
    const response = await authApi.login({
      username: loginForm.username,
      password: loginForm.password
    })

    console.log('로그인 응답:', response)

    // 로그인 성공 처리
    if (response.token && response.username) {
      // JWT 토큰을 localStorage에 저장
      localStorage.setItem('token', response.token)
      localStorage.setItem('username', response.username)

      ElMessage.success(response.message || '로그인 성공!')

      // 게시글 목록으로 이동
      router.push('/posts')
    } else {
      ElMessage.error(response.message || '로그인에 실패했습니다')
    }

  } catch (error) {
    console.error('로그인 에러:', error)

    const apiError = error as ApiError

    // 에러 타입별 처리
    if (apiError.response?.status === 401) {
      ElMessage.error('아이디 또는 비밀번호가 틀렸습니다')
    } else if (apiError.response?.status === 500) {
      ElMessage.error('서버 오류가 발생했습니다')
    } else {
      ElMessage.error('로그인 중 오류가 발생했습니다')
    }
  } finally {
    loading.value = false
  }
}

// 컴포넌트가 마운트될 때 이미 로그인된 상태면 리다이렉트
onMounted(() => {
  const token = localStorage.getItem('token')
  if (token) {
    router.push('/posts')
  }
})
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-main {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-box {
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  padding: 40px;
  width: 100%;
  max-width: 400px;
}

.logo-section {
  text-align: center;
  margin-bottom: 30px;
}

.logo-section h2 {
  color: #303133;
  margin-bottom: 8px;
  font-size: 24px;
  font-weight: 600;
}

.logo-section p {
  color: #606266;
  font-size: 14px;
  margin: 0;
}

.login-form {
  margin-bottom: 20px;
}

.login-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 500;
}

.demo-info {
  margin-top: 20px;
}

.demo-info p {
  margin: 4px 0;
  font-size: 13px;
}

/* 반응형 디자인 */
@media (max-width: 480px) {
  .login-box {
    margin: 20px;
    padding: 30px 20px;
  }

  .logo-section h2 {
    font-size: 20px;
  }
}
</style>
