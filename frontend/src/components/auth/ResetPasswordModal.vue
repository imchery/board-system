<template>
  <el-dialog
      v-model="dialogVisible"
      title="비밀번호 찾기"
      width="500px"
      :close-on-click-modal="false"
  >
    <!--  폼  -->
    <el-form
        ref="resetFormRef"
        :model="resetForm"
        :rules="resetRules"
        label-width="100px"
        label-position="left"
    >
      <!--   아이디   -->
      <el-form-item label="아이디" prop="username">
        <el-input
            v-model="resetForm.username"
            placeholder="아이디를 입력하세요"
            :disabled="isLoading"
        />
      </el-form-item>

      <!--   이메일   -->
      <el-form-item label="이메일" prop="email">
        <el-input
            v-model="resetForm.email"
            placeholder="이메일을 입력하세요"
            :disabled="isLoading || codeSent"
        >
          <template #append>
            <el-button
                @click="handleSendCode"
                :disabled="isLoading || codeSent || cooldown > 0"
                :loading="isSendingCode"
            >
              {{ cooldown > 0 ? `${cooldown}초` : codeSent ? '발송완료' : '인증 코드 발송' }}
            </el-button>
          </template>
        </el-input>
      </el-form-item>

      <!--  인증 코드  -->
      <el-form-item label="인증 코드" prop="verificationCode" v-if="codeSent">
        <el-input
            v-model="resetForm.verificationCode"
            placeholder="6자리 인증 코드를 입력하세요"
            maxlength="6"
            :disabled="isLoading"
        />
      </el-form-item>
    </el-form>

    <!--  안내 문구  -->
    <el-alert
        v-if="codeSent"
        title="이메일로 발송된 인증 코드를 입력해주세요"
        type="info"
        :closeable="true"
        show-icon
        style="margin-bottom: 20px"
    />
    <!--  버튼  -->
    <template #footer>
      <el-button @click="handleClose" :disabled="isLoading">
        취소
      </el-button>
      <el-button
          type="primary"
          @click="handleResetPassword"
          :loading="isLoading"
          :disabled="!codeSent"
      >
        임시 비밀번호 발급
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import {reactive, ref, watch} from "vue";
import {ElMessage, FormInstance, FormRules} from "element-plus";
import {authApi} from "@/api/auth.ts";
import {ResetPasswordRequest} from "@/types/api.ts";

// Props & Emits
const props = defineProps<{
  visible: boolean,
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  'success': []
}>()

// 반응형 데이터
const dialogVisible = ref(false)
const resetFormRef = ref<FormInstance>()
const isLoading = ref(false)
const codeSent = ref(false)
const isSendingCode = ref(false)
const cooldown = ref(0)

// 폼 데이터
const resetForm = reactive({
  username: '',
  email: '',
  verificationCode: '',
})

// 검증 규칙
const resetRules: FormRules = {
  username: [
    {required: true, message: '아이디를 입력해주세요', trigger: 'blur'},
    {min: 4, max: 20, message: '아이디는 4-20자 사이여야 합니다', trigger: 'blur'},
  ],
  email: [
    {required: true, message: '이메일을 입력해주세요', trigger: 'blur'},
    {type: 'email', message: '올바른 이메일 형식이 아닙니다', trigger: 'blur'},
  ],
  verificationCode: [
    {required: true, message: '인증 코드를 입력해주세요', trigger: 'blur'},
    {len: 6, message: '인증 코드는 6자리입니다', trigger: 'blur'},
  ]
}

// Props 변경 감지
watch(() => props.visible, (newVal) => {
  dialogVisible.value = newVal
})

watch(dialogVisible, (newVal) => {
  emit('update:visible', newVal)
  if (!newVal) {
    resetFormData()
  }
})

/**
 * 인증 코드 발송
 */
const handleSendCode = async () => {
  // 아이디, 이메일 검증
  if (!resetFormRef.value) return

  try {
    await resetFormRef.value.validateField(['username', 'email'])
  } catch {
    return
  }

  isSendingCode.value = true

  try {
    await authApi.sendVerificationCode(resetForm.email)
    ElMessage.success('인증 코드가 이메일로 발송되었습니다')
    codeSent.value = true

    // 3분 쿨다운
    startCooldown(180)
  } catch (error: any) {
    const message = error.response?.data?.message || '인증 코드 발송에 실패했습니다'
    ElMessage.error(message)
  } finally {
    isSendingCode.value = false
  }
}

/**
 * 쿨다운 타이머 시작
 * @param seconds
 */
const startCooldown = (seconds: number) => {
  cooldown.value = seconds

  const timer = setInterval(() => {
    cooldown.value--

    if (cooldown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
}

/**
 * 임시 비밀번호 발급
 */
const handleResetPassword = async () => {
  if (!resetFormRef.value) return

  const isValid = await resetFormRef.value.validate().catch(() => false)
  if (!isValid) return

  isLoading.value = true

  try {
    const request: ResetPasswordRequest = {
      username: resetForm.username,
      email: resetForm.email,
      verificationCode: resetForm.verificationCode,
    }

    const response = await authApi.resetPassword(request)

    ElMessage.success({
      message: `임시 비밀번호가 ${response.data?.email}로 발송되었습니다`,
      duration: 5000
    })

    emit('success')
    handleClose()
  } catch (error: any) {
    const message = error.response?.data?.message || '임시 비밀번호 발급에 실패했습니다'
    ElMessage.error(message)
  } finally {
    isLoading.value = false
  }
}

/**
 * 모달 닫기
 */
const handleClose = () => {
  dialogVisible.value = false
}

/**
 * 폼 데이터 초기화
 */
const resetFormData = () => {
  resetForm.username = ''
  resetForm.email = ''
  resetForm.verificationCode = ''
  codeSent.value = false
  cooldown.value = 0
  resetFormRef.value?.clearValidate()
}
</script>

<style scoped></style>
