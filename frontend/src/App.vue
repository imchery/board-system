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
    <router-view v-else/>
  </div>
</template>

<script setup lang="ts">
import {useAuthStore} from "@/stores/auth.ts";
import {Loading} from "@element-plus/icons-vue";
import {onMounted} from "vue";

// 인증 스토어 사용
const authStore = useAuthStore()

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
</style>
