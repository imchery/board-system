<template>
  <el-dialog
      v-model="dialogVisible"
      title="아이디 찾기"
      width="500px"
      :close-on-click-modal="false"
  >
    <!--  1단계: 이메일 입력  -->
    <div v-if="step === 1" class="find-step">
      <p class="step-description">가입하신 이메일 주소로 인증 코드를 보내드립니다.</p>
      <el-form :model="form" ref="formRef">
        <el-form-item label="이메일">
          <div class="email-input-wrapper">
            <el-input
                v-model="form.email"
                placeholder="example@email.com"
                :disabled="codeSent"
            />
            <el-button
                type="primary"
                @click="sendCode"
                :loading="sending"
                :disabled="codeSent && timer > 0"
            >
              {{ getButtonText }}
            </el-button>
          </div>
        </el-form-item>

        <!--   인증 코드 입력 (발송 후 표시)   -->
        <el-form-item v-if="codeSent" label="인증 코드">
          <el-input
              v-model="form.code"
              placeholder="6자리 인증 코드"
              maxlength="6"
          />
          <div class="timer-info">
            <span v-if="timer > 0" class="timer">{{ formatTime(timer) }}</span>
            <span v-else class="timer-expired">인증 시간이 만료되었습니다</span>
          </div>
        </el-form-item>
      </el-form>
    </div>

    <!--  2단계: 아이디 표시  -->
    <div v-else-if="step === 2" class="find-result">
      <el-result icon="success" title="아이디를 찾았습니다">
        <template #sub-title>
          <div class="username-display">
            <p>고객님의 아이디는</p>
            <h3>{{ foundUsername }}</h3>
            <p class="help-text">로그인 화면에서 사용해주세요</p>
          </div>
        </template>
      </el-result>
    </div>

    <!--  버튼 영역  -->
    <template #footer>
      <div v-if="step === 1">
        <el-button @click="closeDialog">취소</el-button>
        <el-button
            type="primary"
            @click="findUsername"
            :loading="loading"
            :disabled="!canSubmit"
        >
          아이디 찾기
        </el-button>
      </div>
      <div v-else>
        <el-button type="primary" @click="goToLogin">로그인하러 가기</el-button>
      </div>
    </template>
  </el-dialog>
</template>
<script setup lang="ts">
import {computed, ref} from "vue"
import {useRouter} from "vue-router"
import {ElMessage} from "element-plus";
import {authApi} from "@/api/auth.ts";

interface Props {
  visible: boolean
}

const props = defineProps<Props>()
const emit = defineEmits<{
  'update:visible': [value: boolean]
}>()

const router = useRouter()

// 반응형 데이터
const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const step = ref(1) // 1. 이메일 입력, 2. 아이디 표시

const form = ref({
  email: '',
  code: ''
})

const formRef = ref()
const sending = ref(false)
const loading = ref(false)
const codeSent = ref(false)
const timer = ref(0)
const foundUsername = ref('')

let timerInterval: number | null = null

// 인증 코드 발송
const sendCode = async () => {
  if (!form.value.email) {
    ElMessage.warning('이메일을 입력해주세요')
    return
  }

  try {
    sending.value = true
    await authApi.sendVerificationCode(form.value.email)

    ElMessage.success('인증 코드가 발송되었습니다')
    codeSent.value = true
    startTimer()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '인증 코드 발송에 실패했습니다')
  } finally {
    sending.value = false
  }
}

// 타이머 시작
const startTimer = () => {
  timer.value = 180 // 3분

  if (timerInterval) {
    clearTimeout(timerInterval)
  }

  timerInterval = window.setInterval(() => {
    timer.value--

    if (timer.value <= 0) {
      if (timerInterval) {
        clearInterval(timerInterval)
      }
      ElMessage.warning('인증 시간이 만료되었습니다. 재발송해주세요.')
    }
  }, 1000)
}

// 아이디 찾기
const findUsername = async () => {
  if (!form.value.code) {
    ElMessage.warning('인증 코드를 입력해주세요')
    return
  }

  try {
    loading.value = true
    const response = await authApi.findUsername(form.value.email, form.value.code)
    if (response.result) {
      foundUsername.value = response.data.maskedUsername
      step.value = 2
      ElMessage.success('아이디를 찾았습니다')
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '아이디 찾기에 실패했습니다')
  } finally {
    loading.value = false
  }
}

const getButtonText = computed(() => {
  if (!codeSent.value) return '인증 코드 발송'
  if (timer.value > 0) return `재발송 (${formatTime(timer.value)})`
  return '재발송'
})

const goToLogin = () => {
  closeDialog()
  router.push({path: '/login'})
}

// 다이얼로그 닫기
const closeDialog = () => {
  dialogVisible.value = false
  resetForm()
}

// 폼 초기화
const resetForm = () => {
  step.value = 1
  form.value = {email: '', code: ''}
  codeSent.value = false
  timer.value = 0
  foundUsername.value = ''

  if (timerInterval) {
    clearTimeout(timerInterval)
  }
}

// 계산된 속성
const canSubmit = computed(() => {
  return codeSent.value && form.value.code.length === 6 && timer.value > 0
})

// 시간 포맷팅
const formatTime = (seconds: number) => {
  const minutes = Math.floor(seconds / 60)
  const secs = seconds % 60

  return `${String(minutes).padStart(2, '0')}:${String(secs).padStart(2, '0')}`

}
</script>
<style scoped>
@import "@/assets/styles/components/findUsernameModal.css";
</style>
