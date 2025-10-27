<template>
  <div class="signup-container">
    <el-container>
      <el-main class="signup-main">
        <div class="signup-box">
          <!-- 로고 영역 -->
          <div class="logo-section">
            <h2>회원가입</h2>
            <p>Board System에 오신 것을 환영합니다</p>
          </div>

          <!-- 회원가입 폼 -->
          <el-form
              ref="signupFormRef"
              :model="signupForm"
              :rules="signupRules"
              class="signup-form"
              label-position="top"
          >
            <!-- 아이디 입력 -->
            <el-form-item label="아이디" prop="username" required>
              <el-input
                  v-model="signupForm.username"
                  placeholder="영문, 숫자 조합 4-20자"
                  @blur="handleUsernameBlur"
              >
                <template #suffix>
                  <el-icon v-if="usernameCheckStatus === 'success'" color="#67C23A">
                    <CircleCheck/>
                  </el-icon>
                  <el-icon v-else-if="usernameCheckStatus === 'error'" color="#F56C6C">
                    <CircleClose/>
                  </el-icon>
                </template>
              </el-input>
              <span v-if="usernameMessage"
                    :class="['check-message', usernameCheckStatus]">
                {{ usernameMessage }}
              </span>
            </el-form-item>

            <!-- 비밀번호 입력 -->
            <el-form-item label="비밀번호" props="password" required>
              <el-input
                  v-model="signupForm.password"
                  type="password"
                  placeholder="영문, 숫자, 특수문자 조합 8-20자"
                  show-password
              />
            </el-form-item>

            <!-- 비밀번호 확인 -->
            <el-form-item label="비밀번호 확인" props="passwordConfirm" required>
              <el-input
                  v-model="signupForm.password"
                  type="password"
                  placeholder="비밀번호를 다시 입력해주세요"
                  show-password
              />
            </el-form-item>

            <!-- 이메일 입력 -->
            <el-form-item label="이메일" prop="email" required>
              <el-input
                  v-model="signupForm.email"
                  placeholder="example@email.com"
                  @blur="handleEmailBlur"
              >
                <template #suffix>
                  <el-icon v-if="emailCheckStatus === 'success'" color="#67C23A">
                    <CircleCheck/>
                  </el-icon>
                  <el-icon v-else-if="emailCheckStatus === 'error'" color="#F56C6C">
                    <CircleClose/>
                  </el-icon>
                </template>
              </el-input>
              <span v-if="emailMessage"
                    :class="['check-message', emailCheckStatus]">
                {{ emailMessage }}
              </span>
            </el-form-item>

            <!-- 닉네임 입력 -->
            <el-form-item label="닉네임" prop="nickname" required>
              <el-input
                  v-model="signupForm.nickname"
                  placeholder="2-10자 이내"
              />
            </el-form-item>

            <!-- 회원가입 버튼 -->
            <el-form-item>
              <el-button
                  type="primary"
                  size="large"
                  :loading="isLoading"
                  @click="handleSignup"
                  class="signup-button"
              >
                {{ isLoading ? '회원가입 중...' : '회원가입' }}
              </el-button>
            </el-form-item>
          </el-form>

          <!-- 로그인 링크 -->
          <div class="login-link">
            <span>이미 계정이 있으신가요?</span>
            <el-button type="text" @click="goToLogin">로그인</el-button>
          </div>
        </div>
      </el-main>
    </el-container>
  </div>
</template>
<script setup lang="ts">
import {reactive, ref} from "vue";
import {ElMessage, FormInstance, FormRules} from "element-plus";
import {useRouter} from 'vue-router'
import {authApi} from "@/api/auth.ts";
import {SignupRequest} from "@/types/api.ts";

// ===================== 초기화 =====================
const router = useRouter()
const signupFormRef = ref<FormInstance>()
const isLoading = ref(false)

// 중복 체크 상태
const usernameCheckStatus = ref<'success' | 'error' | ''>('')
const usernameMessage = ref('')
const emailCheckStatus = ref<'success' | 'error' | ''>('')
const emailMessage = ref('')

// ===================== 폼 데이터 =====================
const signupForm = reactive({
  username: '',
  password: '',
  passwordConfirm: '',
  email: '',
  nickname: '',
})

// ===================== Validation 규칙 =====================
const signupRules: FormRules = {
  username: [
    {required: true, message: '아이디를 입력해주세요', trigger: 'blur'},
    {min: 4, max: 20, message: '아이디는 4-20자 사이여야 합니다', trigger: 'blur'},
    {
      pattern: /^[a-zA-Z0-9]+$/,
      message: '영문과 숫자만 사용 가능합니다',
      trigger: 'blur'
    },
  ],
  password: [
    {required: true, message: '비밀번호를 입력해주세요', trigger: 'blur'},
    {min: 8, max: 20, message: '비밀번호는 8-20자 사이여야 합니다', trigger: 'blur'},
    {
      pattern: /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]+$/,
      message: '영문과 숫자, 특수문자를 모두 포함해야 합니다',
      trigger: 'blur'
    },
  ],
  passwordConfirm: [
    {required: true, message: '비밀번호 확인을 입력해주세요', trigger: 'blur'},
    {
      validator: (rule, value, callback) => {
        if (value != signupForm.password) {
          callback(new Error('비밀번호가 일치하지 않습니다'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  email: [
    {required: true, message: '이메일을 확인을 입력해주세요', trigger: 'blur'},
    {type: 'email', message: '올바른 이메일 형식이 아닙니다', trigger: 'blur'},
  ],
  nickname: [
    {required: true, message: '닉네임을 입력해주세요', trigger: 'blur'},
    {min: 2, max: 10, message: '닉네임은 2-10자 사이여야 합니다', trigger: 'blur'},

  ],
}

// ===================== 아이디 중복 체크 =====================
const handleUsernameBlur = async () => {
  // 아이디 형식 검증 먼저
  if (!signupForm.username) return
  if (signupForm.username.length < 4 || signupForm.username.length > 20) return
  if (!/^[a-zA-Z0-9]+$/.test(signupForm.username)) return

  try {
    const available = await authApi.checkUsername(signupForm.username)

    if (available) {
      usernameCheckStatus.value = 'success'
      usernameMessage.value = '사용 가능한 아이디입니다'
    } else {
      usernameCheckStatus.value = 'error'
      usernameMessage.value = '이미 사용 중인 아이디입니다'
    }
  } catch (error) {
    console.error('아이디 중복 체크 실패:', error)
    usernameCheckStatus.value = 'error'
    usernameMessage.value = '중복 체크 중 오류가 발생했습니다'
  }
}

// ===================== 이메일 중복 체크 =====================
const handleEmailBlur = async () => {
  // 이메일 형식 검증 먼저
  if (!signupForm.email) return
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(signupForm.email)) return

  try {
    const available = await authApi.checkEmail(signupForm.email)

    if (available) {
      emailCheckStatus.value = 'success'
      emailMessage.value = '사용 가능한 이메일입니다'
    } else {
      emailCheckStatus.value = 'error'
      emailMessage.value = '이미 사용 중인 이메일입니다'
    }
  } catch (error) {
    console.error('이메일 중복 체크 실패:', error)
    emailCheckStatus.value = 'error'
    emailMessage.value = '중복 체크 중 오류가 발생했습니다'
  }
}

// ===================== 회원가입 처리 =====================
const handleSignup = async () => {
  // 1. 폼 검증
  if (!signupFormRef.value) return

  const isValid = await signupFormRef.value.validate().catch(() => false)
  if (!isValid) return

  // 2. 중복 체크 확인
  if (usernameCheckStatus.value !== 'success') {
    ElMessage.warning('아이디 중복 체크를 완료해주세요')
    return
  }

  if (emailMessage.value !== 'success') {
    ElMessage.warning('이메일 중복 체크를 완료해주세요')
    return
  }

  // 3. 회원가입 요청
  isLoading.value = true

  try {
    const signupData: SignupRequest = {
      username: signupForm.username,
      password: signupForm.password,
      email: signupForm.email,
      nickname: signupForm.nickname
    }

    const response = await authApi.signup(signupData)
    ElMessage.success(response.data.message || '회원가입이 완료되었습니다!')

    // 2초 후 로그인 페이지로 이동
    setTimeout(() => {
      router.push('/login')
    }, 2000)
  } catch (error: any) {
    console.error('회원가입 실패:', error)

    const errorMessage = error.response?.data?.message || '회원가입에 실패했습니다'
    ElMessage.error(errorMessage)
  } finally {
    isLoading.value = false
  }
}

// 로그인 페이지로 이동
const goToLogin = () => {
  router.push('/login')
}
</script>
<style scoped>
@import '@/assets/styles/components/signup.css';
</style>
