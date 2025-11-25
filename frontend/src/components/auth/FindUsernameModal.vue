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
      <el-form :model="form" :rules="formRules" ref="formRef">
        <el-form-item label="이메일" prop="email">
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
                :disabled="!canSend"
            >
              {{ getButtonText }}
            </el-button>
          </div>
        </el-form-item>

        <!--   인증 코드 입력 (발송 후 표시)   -->
        <el-form-item v-if="codeSent" label="인증 코드" prop="code">
          <el-input
              v-model="form.code"
              placeholder="6자리 인증 코드"
              maxlength="6"
          />
          <div class="timer-info">
            <span v-if="cooldown > 0" class="timer">{{ formatRemainingTime(cooldown) }} 후 재발송 가능</span>
            <span v-else class="timer-expired">재발송 가능합니다</span>
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
import {computed, onUnmounted, ref, watch} from "vue"
import {useRouter} from "vue-router"
import {ElMessage, FormRules} from "element-plus";
import {authApi} from "@/api/auth.ts";
import {
  canSendVerificationCode,
  formatRemainingTime,
  recordVerificationCodeSent,
  VerificationCodeType
} from "@/utils/verificationCodeUtils.ts";

interface Props {
  visible: boolean
}

const props = defineProps<Props>()
const emit = defineEmits<{
  'update:visible': [value: boolean]
}>()

const router = useRouter()
const VERIFICATION_TYPE: VerificationCodeType = 'findUsername'

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
const cooldown = ref(0)
const foundUsername = ref('')

let cooldownInterval: number | null = null

const formRules: FormRules = {
  email: [
    {required: true, message: '이메일을 입력해주세요', trigger: 'blur'},
    {type: 'email', message: '올바른 이메일 형식이 아닙니다', trigger: 'blur'},
  ],
  code: [
    {required: true, message: '인증 코드를 입력해주세요', trigger: 'blur'},
    {len: 6, message: '인증 코드는 6자리입니다', trigger: 'blur'},
  ]
}

// 모달이 열릴 때 쿨다운 체크
watch(() => props.visible, (newVal) => {
  if (newVal && isValidEmail(form.value.email)) {
    checkCooldown()
  }
})

// 이메일 검증
const isValidEmail = (email: string): boolean => {
  if (!email) return false
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
}

// 쿨다운 체크
const checkCooldown = () => {
  const {canSend, remainingSeconds} = canSendVerificationCode(form.value.email, VERIFICATION_TYPE)

  if (!canSend) {
    cooldown.value = remainingSeconds
    codeSent.value = true
    startCooldownTimer()
  }
}

// 인증 코드 발송
const sendCode = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validateField('email')
  } catch {
    return
  }

  // 발송 전 쿨다운 체크
  const {canSend, remainingSeconds} = canSendVerificationCode(form.value.email, VERIFICATION_TYPE)

  if (!canSend) {
    ElMessage.warning(`${formatRemainingTime(remainingSeconds)} 후에 재발송할 수 있습니다`)
    return
  }

  try {
    sending.value = true
    await authApi.sendVerificationCode(form.value.email)

    ElMessage.success('인증 코드가 발송되었습니다')
    // 발송 기록 저장
    recordVerificationCodeSent(form.value.email, VERIFICATION_TYPE)

    codeSent.value = true
    cooldown.value = 180
    startCooldownTimer()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '인증 코드 발송에 실패했습니다')
  } finally {
    sending.value = false
  }
}

// 타이머 시작
const startCooldownTimer = () => {
  // 기존 타이머 정리
  if (cooldownInterval) {
    clearTimeout(cooldownInterval)
    cooldownInterval = null
  }

  cooldownInterval = window.setInterval(() => {
    cooldown.value--

    if (cooldown.value <= 0) {
      if (cooldownInterval) {
        clearInterval(cooldownInterval)
        cooldownInterval = null
      }
      codeSent.value = false
      form.value.code = ''
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

// 발송 가능 여부
const canSend = computed(() => {
  if (!form.value.email) return false
  if (sending.value) return false

  const {canSend} = canSendVerificationCode(form.value.email, VERIFICATION_TYPE)
  return canSend
})

// 버튼 텍스트
const getButtonText = computed(() => {
  if (!codeSent.value) return '인증 코드 발송'
  if (cooldown.value > 0) return `재발송 (${formatRemainingTime(cooldown.value)})`
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
  cooldown.value = 0
  foundUsername.value = ''

  if (cooldownInterval) {
    clearTimeout(cooldownInterval)
    cooldownInterval = null
  }
}

// 계산된 속성
const canSubmit = computed(() => {
  return codeSent.value && form.value.code.length === 6
})

// 컴포넌트 언마운트 시 타이머 정리
onUnmounted(() => {
  if (cooldownInterval) {
    clearTimeout(cooldownInterval)
    cooldownInterval = null
  }
})
</script>
<style scoped>
@import "@/assets/styles/components/findUsernameModal.css";
</style>
