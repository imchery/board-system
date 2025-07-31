<template>
  <header class="app-header">
    <el-container>
      <el-header class="header-content">
        <!--    로고/브랜드 영역    -->
        <div class="brand-section">
          <router-link to="/" class="brand-link">
            <div class="brand-content">
              <span class="brand-icon">📝</span>
              <span class="brand-text">Board System</span>
            </div>
          </router-link>
        </div>

        <!--    네비게이션 메뉴    -->
<!--        <nav class="nav-section">-->
<!--          <el-menu-->
<!--              mode="horizontal"-->
<!--              :default-active="activeIndex"-->
<!--              class="nav-menu"-->
<!--              @select="handleMenuSelect"-->
<!--          >-->
<!--            <el-menu-item index="/posts">-->
<!--              <el-icon>-->
<!--                <Document/>-->
<!--              </el-icon>-->
<!--              게시글-->
<!--            </el-menu-item>-->

<!--            <el-menu-item index="/posts/create" v-if="authStore.isLoggedIn">-->
<!--              <el-icon>-->
<!--                <Edit/>-->
<!--              </el-icon>-->
<!--              글쓰기-->
<!--            </el-menu-item>-->
<!--          </el-menu>-->
<!--        </nav>-->

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
import {ArrowDown, Document, Edit, SwitchButton, User, UserFilled} from "@element-plus/icons-vue";
import {useRoute, useRouter} from "vue-router";
import {useAuthStore} from "@/stores/auth.ts";
import {computed} from "vue";
import {ElMessage, ElMessageBox} from "element-plus";

// Router & Auth Store
const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

console.log("router =====================", router)
console.log("route =====================", route)

// 현재 활성 메뉴 인덱스
const activeIndex = computed(() => {
  return route.path
})

// 메뉴 선택 처리
const handleMenuSelect = (index: string) => {
  return router.push(index)
}

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

      // 로그인 페이지로 이동
      router.push({path: '/login'})
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
.app-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
  border-bottom: none;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  position: sticky;
  top: 0;
  z-index: 1000;
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 70px;
  padding: 0 30px;
  max-width: 1200px;
  margin: 0 auto;
  position: relative;
}

/* 브랜드 영역 */
.brand-section {
  flex-shrink: 0;
}

.brand-link {
  text-decoration: none;
  color: inherit;
}

.brand-content {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 12px 24px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(15px);
  border-radius: 30px;
  border: 1px solid rgba(255, 255, 255, 0.25);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.brand-content:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: translateY(-3px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.2);
}

.brand-icon {
  font-size: 28px;
  filter: drop-shadow(2px 2px 4px rgba(0, 0, 0, 0.1));
}

.brand-text {
  font-size: 20px;
  font-weight: 800;
  color: white;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.2);
  background: linear-gradient(45deg, #ffffff, #f8f9fa);
  background-size: 400% 400%;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  animation: shimmer 3s ease-in-out infinite;
}

@keyframes shimmer {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

/* 네비게이션 영역 */
.nav-section {
  flex: 1;
  display: flex;
  justify-content: center;
}

.nav-menu {
  border-bottom: none;
  background: transparent;
  position: relative;
}

.nav-menu::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: calc(100% + 20px);
  height: 60px;
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(15px);
  border-radius: 30px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  z-index: -1;
}

.nav-menu .el-menu-item {
  padding: 0 25px;
  height: 50px;
  line-height: 50px;
  color: white;
  font-weight: 600;
  border-radius: 25px;
  margin: 0 8px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.nav-menu .el-menu-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent);
  transition: left 0.5s;
}

.nav-menu .el-menu-item:hover::before {
  left: 100%;
}

.nav-menu .el-menu-item:hover {
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(15px);
  transform: translateY(-3px) scale(1.05);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.2);
}

.nav-menu .el-menu-item.is-active {
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  transform: translateY(-2px);
}

/* 사용자 영역 */
.user-section {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.welcome-text {
  font-size: 15px;
  color: rgba(255, 255, 255, 0.95);
  white-space: nowrap;
  font-weight: 600;
  text-shadow: 1px 1px 3px rgba(0, 0, 0, 0.2);
  margin-right: 15px;
}

.auth-buttons {
  display: flex;
  gap: 12px;
}

.auth-buttons .el-button {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(15px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
  border-radius: 30px;
  padding: 12px 24px;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  position: relative;
  overflow: hidden;
}

.auth-buttons .el-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.6s;
}

.auth-buttons .el-button:hover::before {
  left: 100%;
}

.auth-buttons .el-button:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: translateY(-3px) scale(1.05);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.3);
  border-color: rgba(255, 255, 255, 0.5);
}

.user-dropdown-btn {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(15px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
  border-radius: 30px;
  padding: 12px 24px;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  position: relative;
  overflow: hidden;
}

.user-dropdown-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.6s;
}

.user-dropdown-btn:hover::before {
  left: 100%;
}

.user-dropdown-btn:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: translateY(-3px) scale(1.05);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.3);
  border-color: rgba(255, 255, 255, 0.5);
}

/* 반응형 디자인 */
@media (max-width: 768px) {
  .header-content {
    padding: 0 20px;
    height: 65px;
  }

  .welcome-text {
    display: none; /* 모바일에서는 환영 메시지 숨김 */
  }

  .brand-content {
    padding: 10px 16px;
    gap: 10px;
  }

  .brand-icon {
    font-size: 24px;
  }

  .brand-text {
    font-size: 16px;
  }

  .nav-menu .el-menu-item {
    padding: 0 15px;
    margin: 0 4px;
  }

  .auth-buttons .el-button,
  .user-dropdown-btn {
    padding: 10px 16px;
    font-size: 13px;
  }
}
</style>
