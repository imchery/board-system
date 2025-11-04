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
                  <el-icon v-if="validation.username.status === 'success'" color="#67C23A">
                    <CircleCheck/>
                  </el-icon>
                  <el-icon v-else-if="validation.username.status === 'error'" color="#F56C6C">
                    <CircleClose/>
                  </el-icon>
                </template>
              </el-input>
              <span v-if="validation.username.message"
                    :class="['check-message', validation.username.status]">
                {{ validation.username.message }}
              </span>
            </el-form-item>

            <!-- 비밀번호 입력 -->
            <el-form-item label="비밀번호" prop="password" required>
              <el-input
                  v-model="signupForm.password"
                  type="password"
                  placeholder="영문, 숫자, 특수문자 조합 8-20자"
                  show-password
              />
            </el-form-item>

            <!-- 비밀번호 확인 -->
            <el-form-item label="비밀번호 확인" prop="passwordConfirm" required>
              <el-input
                  v-model="signupForm.passwordConfirm"
                  type="password"
                  placeholder="비밀번호를 다시 입력해주세요"
                  show-password
              />
            </el-form-item>

            <!-- 이메일 입력 -->
            <el-form-item label="이메일" prop="email" required>
              <div class="email-input-wrapper">
                <el-input
                    v-model="signupForm.email"
                    placeholder="example@email.com"
                    @blur="handleEmailBlur"
                    :disabled="emailVerification.verified"
                >
                  <template #suffix>
                    <el-icon v-if="validation.email.status === 'success'" color="#67C23A">
                      <CircleCheck/>
                    </el-icon>
                    <el-icon v-else-if="validation.email.status === 'error'" color="#F56C6C">
                      <CircleClose/>
                    </el-icon>
                  </template>
                </el-input>

                <!-- 인증 코드 발송 버튼 -->
                <el-button
                    type="primary"
                    :disabled="validation.email.status !== 'success' || !emailVerification.canResend"
                    :loading="emailVerification.sending"
                    @click="handleSendCode"
                    class="send-code-btn"
                >
                  {{ getSendButtonText() }}
                </el-button>
              </div>

              <span v-if="validation.email.message"
                    :class="['check-message', validation.email.status]">
                {{ validation.email.message }}
              </span>
            </el-form-item>

            <!-- 인증 코드 입력 영역(코드 발송 후에만 표시) -->
            <el-form-item v-if="emailVerification.codeSent" label="인증 코드" required>
              <div class="verification-input-wrapper">
                <el-input
                    v-model="emailVerification.code"
                    placeholder="6자리 인증 코드 입력"
                    maxlength="6"
                    :disabled="emailVerification.verified"
                >
                  <template #suffix>
                    <el-icon v-if="emailVerification.verified" color="#67C23A">
                      <CircleCheck/>
                    </el-icon>
                  </template>
                </el-input>

                <!-- 인증 확인 버튼 -->
                <el-button
                    type="success"
                    :disabled="emailVerification.verified || emailVerification.code.length !== 6"
                    :loading="emailVerification.verifying"
                    @click="handleVerifyCode"
                    class="verify-btn"
                >
                  {{ emailVerification.verified ? '인증 완료' : '인증 확인' }}
                </el-button>
              </div>

              <!-- 타이머 & 재발송 -->
              <div class="verification-info">
                <span v-if="!emailVerification.verified && emailVerification.timer > 0" class="timer">
                  ⏱재발송 가능: {{ formatTime(emailVerification.timer) }}
                </span>
                <span v-else-if="!emailVerification.verified && emailVerification.timer <= 0" class="timer-ready">
                  재발송 가능
                </span>
              </div>

              <span v-if="emailVerification.verified" class="check-message success">
                이메일 인증이 완료되었습니다
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
import {onMounted, reactive, ref} from "vue";
import {ElMessage, FormInstance, FormRules} from "element-plus";
import {useRouter} from 'vue-router'
import {authApi} from "@/api/auth.ts";
import {SignupRequest} from "@/types/api.ts";

// ===================== 초기화 =====================
const router = useRouter()
const signupFormRef = ref<FormInstance>()
const isLoading = ref(false)

// 타이머 인터벌 ID
let resendTimerInterval: number | null = null

// 중복 체크 상태
type ValidationState = 'idle' | 'success' | 'error'

const validation = reactive({
  username: {
    status: 'idle' as ValidationState,
    message: ''
  },
  email: {
    status: 'idle' as ValidationState,
    message: ''
  }
})

// 이메일 인증 상태
const emailVerification = reactive({
  codeSent: false,  // 코드 발송 여부
  code: '',         // 입력한 인증 코드
  verified: false,  // 인증 완료 여부
  timer: 180,       // 유효시간 3분
  canResend: false, // 재발송 가능 여부
  sending: false,   // 발송 중
  verifying: false, // 검증 중
})

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
      pattern: /^(?=.*[a-zA-Z])(?=.*[0-9])[a-zA-Z0-9]+$/,
      message: '영문과 숫자를 모두 포함해야 합니다',
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
        if (value !== signupForm.password) {
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
  if (!signupForm.username) {
    validation.username.status = 'idle'
    validation.username.message = ''
    return
  }

  if (signupForm.username.length < 4 || signupForm.username.length > 20) {
    validation.username.status = 'idle'
    validation.username.message = ''
    return
  }
  if (!/^(?=.*[a-zA-Z])(?=.*[0-9])[a-zA-Z0-9]+$/.test(signupForm.username)) {
    validation.username.status = 'idle'
    validation.username.message = ''
    return
  }

  try {
    const available = await authApi.checkUsername(signupForm.username)

    if (available) {
      validation.username.status = 'success'
      validation.username.message = '사용 가능한 아이디입니다'
    } else {
      validation.username.status = 'error'
      validation.username.message = '이미 사용 중인 아이디입니다'
    }
  } catch (error) {
    console.error('아이디 중복 체크 실패:', error)
    validation.username.status = 'error'
    validation.username.message = '중복 체크 중 오류가 발생했습니다'
  }
}

// ===================== 이메일 중복 체크 =====================
const handleEmailBlur = async () => {
  // 이메일 형식 검증 먼저
  if (!signupForm.email) {
    validation.email.status = 'idle'
    validation.email.message = ''
    return
  }

  // 이메일 형식이 틀리면 중복 체크 X
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(signupForm.email)) {
    validation.email.status = 'idle'
    validation.email.message = ''
    return
  }

  try {
    const available = await authApi.checkEmail(signupForm.email)

    if (available) {
      validation.email.status = 'success'
      validation.email.message = '사용 가능한 이메일입니다'
    } else {
      validation.email.status = 'error'
      validation.email.message = '이미 사용 중인 이메일입니다'
    }
  } catch (error) {
    console.error('이메일 중복 체크 실패:', error)
    validation.email.status = 'error'
    validation.email.message = '중복 체크 중 오류가 발생했습니다'
  }
}

// ===================== 이메일 인증 코드 발송 =====================
const handleSendCode = async () => {
  emailVerification.sending = true

  try {
    await authApi.sendVerifivationCode(signupForm.email)

    ElMessage.success('인증 코드가 발송되었습니다. 이메일을 확인해주세요.')

    // 상태 업데이트
    emailVerification.codeSent = true
    emailVerification.timer = 180
    emailVerification.canResend = false

    // 재발송 타이머 시작
    startResendTimer()
  } catch (error: any) {
    console.error('인증 코드 발송 실패:', error)
    const errorMessage = error.response?.data?.message || '인증 코드 발송에 실패했습니다'
    ElMessage.error(errorMessage)
  } finally {
    emailVerification.sending = false
  }
}

// ===================== 인증 코드 검증 =====================
const handleVerifyCode = async () => {
  emailVerification.verifying = true

  try {
    const response = await authApi.verifyCode(signupForm.email, emailVerification.code)
    if (response.data) {
      ElMessage.success('이메일 인증이 완료되었습니다!')
      emailVerification.verified = true

      // 타이머 중지
      if (resendTimerInterval) {
        clearTimeout(resendTimerInterval)
        resendTimerInterval = null
      }
    }
  } catch (error: any) {
    console.error('인증 코드 검증 실패:', error)
    const errorMessage = error.response?.data?.message || '인증 코드가 일치하지 않습니다'
    ElMessage.error(errorMessage)
  } finally {
    emailVerification.verifying = false
  }
}

// ===================== 재발송 타이머 =====================
/**
 * 재발송 타이머 시작
 * - 3분(180초) 카운트다운
 * - 0초가 되면 재발송 가능
 */
const startResendTimer = () => {
  // 1. 기존 타이머 정리
  if (resendTimerInterval) {
    clearInterval(resendTimerInterval)
    resendTimerInterval = null
  }

  // 2. 새 타이머 시작
  resendTimerInterval = window.setInterval(() => {
    emailVerification.timer--

    if (emailVerification.timer <= 0) {
      // 3. 타이머 종료
      if (resendTimerInterval != null) {
        clearInterval(resendTimerInterval)
        resendTimerInterval = null
      }

      emailVerification.canResend = true
      ElMessage.success('이제 인증 코드를 재발송할 수 있습니다')
    }
  }, 1000)
}

// 발송 버튼 텍스트
const getSendButtonText = (): string => {
  if (!emailVerification.codeSent) {
    return '인증 코드 발송'
  }

  if (emailVerification.timer > 0) {
    return `재발송 (${formatTime(emailVerification.timer)})`
  }

  return '재발송'
}

// 시간 포맷팅(160초 -> 03:00)
const formatTime = (seconds: number): string => {
  const minutes = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${String(minutes).padStart(2, '0')}:${String(secs).padStart(2, '0')}`
}

// ===================== 회원가입 처리 =====================
const handleSignup = async () => {
  // 1. 폼 검증
  if (!signupFormRef.value) return

  const isValid = await signupFormRef.value.validate().catch(() => false)
  if (!isValid) return

  // 2. 중복 체크 확인
  if (validation.username.status !== 'success') {
    ElMessage.warning('아이디 중복 체크를 완료해주세요')
    return
  }

  if (validation.email.status !== 'success') {
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

// 컴포넌트 언마운트 시 타이머 정리
onMounted(() => {
  if (resendTimerInterval) {
    clearInterval(resendTimerInterval)
    resendTimerInterval = null
  }
})
</script>
<style>
@import '@/assets/styles/components/signup.css';
</style>
