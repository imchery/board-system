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
        <nav class="nav-section">
          <el-menu
              mode="horizontal"
              :default-active="activeIndex"
              class="nav-menu"
              @select="handleMenuSelect"
          >
            <el-menu-item index="/posts">
              <el-icon>
                <Document/>
              </el-icon>
              게시글
            </el-menu-item>

            <el-menu-item index="/posts/create" v-if="authStore.isLoggedIn">
              <el-icon>
                <Edit/>
              </el-icon>
              글쓰기
            </el-menu-item>
          </el-menu>
        </nav>

        <!--    사용자 영역    -->
        <div class="user-section">
          <!--     로그인된 상태     -->
          <div v-if="authStore.isLoggedIn" class="user-info">
            <span class="welcome-text">
              {{ authStore.currentUser }}님 환영합니다!
            </span>

            <el-dropdown @commend="handleUserCommand">
              <el-button type="primary" size="small">
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
            <el-button @click="goToLogin" v-if="authStore.isLoggedIn">
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
  background: white;
  border-bottom: 1px solid #e4e7ed;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
  padding: 0 20px;
  max-width: 1200px;
  margin: 0 auto;
}

/* 브랜드 영역 */
.brand-section {
  flex-shrink: 0;
}

.brand-link {
  text-decoration: none;
  color: inherit;
}

.brand-link h1 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #409eff;
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
}

.nav-menu .el-menu-item {
  padding: 0 16px;
  height: 50px;
  line-height: 50px;
}

.nav-menu .el-menu-item:hover {
  background-color: #ecf5ff;
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
  font-size: 14px;
  color: #606266;
  white-space: nowrap;
}

.auth-buttons {
  display: flex;
  gap: 8px;
}

/* 반응형 디자인 */
@media (max-width: 768px) {
  .header-content {
    padding: 0 16px;
  }

  .welcome-text {
    display: none; /* 모바일에서는 환영 메시지 숨김 */
  }

  .brand-link h1 {
    font-size: 18px;
  }

  .nav-menu .el-menu-item {
    padding: 0 12px;
  }
}
</style>
