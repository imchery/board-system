<template>
  <div class="home">
    <!--  Element Plus 컨테이너  -->
    <el-container>
      <!--  헤더    -->
      <el-header class="header">
        <div class="header-content">
          <h1>📝 Board System</h1>
          <div class="nav-buttons">
            <el-button type="primary" @click="goToPostList">
              게시글 목록
            </el-button>
            <el-button @click="goToLogin">
              로그인
            </el-button>
          </div>
        </div>
      </el-header>

      <!--  메인 콘텐츠   -->
      <el-main class="main-content">
        <div class="welcome-section">
          <h2>🎉 Vue 3 + Spring Boot MSA 게시판</h2>
          <p>최신 기술 스택으로 만든 게시판 시스템입니다</p>

          <!--     카드 형태의 기능 소개     -->
          <el-row :gutter="20" class="feature-cards">
            <el-col :span="8">
              <el-card shadow="hover">
                <template #header>
                  <span>✨ 최신 기술 </span>
                </template>
                <div>Vue 2 + TypeScript + Element Plus</div>
              </el-card>
            </el-col>

            <el-col :span="8">
              <el-card shadow="hover">
                <template #header>
                  <span>🔐 보안</span>
                </template>
                <div>JWT 토큰 기반 인증</div>
              </el-card>
            </el-col>

            <el-col :span="8">
              <el-card shadow="hover">
                <template #header>
                  <spn>⚡ 성능</spn>
                </template>
                <div>MSA 구조 + MongoDB</div>
              </el-card>
            </el-col>
          </el-row>

          <!--     시작하기 버튼     -->
          <div class="action-section">
            <el-button type="primary" size="large" @click="goToPostList">
              🚀 게시판 둘러보기
            </el-button>
          </div>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
// Vue Router 사용
import {useRouter} from "vue-router";

const router = useRouter()

const goToPostList = () => {
  router.push('/posts')
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.home {
  min-height: 100vh;
}

.header {
  background-color: #409eff;
  color: white;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.nav-buttons {
  display: flex;
  gap: 10px;
}

.main-content {
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.welcome-section {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
  text-align: center;
}

.welcome-section h2 {
  color: #303133;
  margin-bottom: 10px;
}

.welcome-section p {
  color: #606266;
  font-size: 16px;
  margin-bottom: 40px;
}

.feature-cards {
  margin: 40px 0;
}

.action-section {
  margin-top: 40px;
}
</style>
<script setup lang="ts">
</script>
