<template>
  <div id="app">
    <!--  로딩 화면 (인증 상태 확인중)  -->
    <div v-if="authStore.isLoading" class="app-loading">
      <el-icon class="is-loading">
        <Loading/>
      </el-icon>
      <p>로딩 중...</p>
    </div>

    <!--  메인 앱  -->
    <div v-else class="app-layout">
      <!--   헤더(홈, 로그인 페이지가 아닐 때만 표시)   -->
      <Header v-if="!isSpecialPage"/>

      <!--   메인 콘텐츠   -->
      <main class="main-content" :class="{'no-header': isSpecialPage}">
        <router-view/>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import {useAuthStore} from "@/stores/auth.ts";
import {Loading} from "@element-plus/icons-vue";
import {computed, onMounted} from "vue";
import Header from "@/components/Header.vue";
import {useRoute} from "vue-router";

// Router & Auth Store
const route = useRoute()
const authStore = useAuthStore()

// 로그인 페이지인지 확인
const isSpecialPage = computed(() => {
  return route.path === '/login' || route.path === '/'
})

// 앱 시작 시 인증 상태 초기화
onMounted(async () => {
  console.log('앱 시작 - 인증 상태 확인')
  await authStore.initializeAuth()
  console.log('인증 초기화 완료')
})
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  min-height: 100vh;
}

/* Element Plus와 충돌 방지 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

/* 로딩 화면 */
.app-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  font-size: 16px;
  color: #606266;
}

.app-loading .el-icon {
  font-size: 32px;
  margin-bottom: 16px;
  color: #409eff;
}

/* 앱 레이아웃 */
.app-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px); /* 헤더 높이 제외 */
}

.main-content.no-header {
  min-height: 100vh; /* 헤더 없을 때는 전체 높이 */
}

/* 전역 스타일 */
.page-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
</style>
