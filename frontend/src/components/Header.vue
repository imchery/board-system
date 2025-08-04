<template>
  <header class="app-header">
    <el-container>
      <el-header class="header-content">
        <!--    로고/브랜드 영역    -->
        <div class="brand-section">
          <router-link to="/" class="brand-link">
            <div class="brand-content">
              <span class="brand-text">Board System</span>
            </div>
          </router-link>
        </div>

        <!--    사용자 영역    -->
        <div class="user-section">
          <!--     로그인된 상태     -->
          <div v-if="authStore.isLoggedIn" class="user-info">
            <span class="welcome-text">
              {{ authStore.currentUser }}님 환영합니다!
            </span>

            <el-dropdown @command="handleUserCommand">
              <el-button class="user-dropdown-btn">
                <el-icon>
                  <User/>
                </el-icon>
                {{ authStore.currentUser }}
                <el-icon class="el-icon--right">
                  <ArrowDown/>
                </el-icon>
              </el-button>

              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">
                    <el-icon>
                      <User/>
                    </el-icon>
                    프로필
                  </el-dropdown-item>
                  <el-dropdown-item command="logout" divided>
                    <el-icon>
                      <SwitchButton/>
                    </el-icon>
                    로그아웃
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>

          <!--    로그인되지 않은 상태    -->
          <div v-else class="auth-buttons">
            <el-button @click="goToLogin">
              <el-icon>
                <UserFilled/>
              </el-icon>
              로그인
            </el-button>
          </div>
        </div>
      </el-header>
    </el-container>
  </header>
</template>

<script setup lang="ts">
import {ArrowDown, SwitchButton, User, UserFilled} from "@element-plus/icons-vue";
import {useRouter} from "vue-router";
import {useAuthStore} from "@/stores/auth.ts";
import {ElMessage, ElMessageBox} from "element-plus";

// Router & Auth Store
const router = useRouter()
const authStore = useAuthStore()

// 사용자 드롭다운 명령 처리
const handleUserCommand = async (command: string) => {
  switch (command) {
    case 'profile':
      ElMessage.info('프로필 페이지는 준비 중입니다')
      break

    case 'logout':
      await handleLoggout()
      break
  }
}

// 로그아웃 처리
const handleLoggout = async () => {
  try {
    const result = await ElMessageBox.confirm(
        '정말 로그아웃 하시겠습니까?',
        '로그아웃 확인',
        {
          confirmButtonText: '로그아웃',
          cancelButtonText: '취소',
          type: 'warning'
        }
    )

    if (result === 'confirm') {
      // Pinia 스토어를 통한 로그아웃
      authStore.logout()

      ElMessage.success('로그아웃되었습니다')

      // 홈으로 이동
      router.push({path: '/'})
    }
  } catch (error) {
    // 취소를 누른 경우(에러X)
    console.log('로그아웃 취소됨')

  }
}

// 로그인 페이지로 이동
const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
@import "@/assets/styles/components/header.css";
</style>
