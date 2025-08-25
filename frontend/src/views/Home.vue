<template>
  <div class="home">
    <!-- 배경 애니메이션 요소들 -->
    <div class="background-elements">
      <div class="floating-shape shape-1"></div>
      <div class="floating-shape shape-2"></div>
      <div class="floating-shape shape-3"></div>
      <div class="floating-shape shape-4"></div>
    </div>

    <!-- 메인 히어로 섹션 -->
    <div class="hero-section">
      <div class="hero-content">
        <!-- 메인 타이틀 -->
        <div class="title-section">
          <h1 class="main-title">
            <span class="title-line gradient-text">Board System</span>
          </h1>
          <p class="subtitle">
            차세대 게시판 플랫폼으로 새로운 경험을 시작하세요
          </p>
        </div>

        <!-- 기술 스택 배지들 -->
        <div class="tech-badges">
          <div class="badge badge-vue">
            <span class="badge-icon">⚡</span>
            <span>Vue 3</span>
          </div>
          <div class="badge badge-spring">
            <span class="badge-icon">🍃</span>
            <span>Spring Boot</span>
          </div>
          <div class="badge badge-mongo">
            <span class="badge-icon">🌿</span>
            <span>MongoDB</span>
          </div>
          <div class="badge badge-jwt">
            <span class="badge-icon">🔐</span>
            <span>JWT Auth</span>
          </div>
        </div>

        <!-- 기능 카드들 -->
        <div class="features-grid">
          <div class="feature-card" @mouseenter="handleCardHover" @mouseleave="handleCardLeave">
            <div class="feature-icon">🚀</div>
            <h3>Lightning Fast</h3>
            <p>MSA 아키텍처로 구현된 초고속 성능</p>
          </div>

          <div class="feature-card" @mouseenter="handleCardHover" @mouseleave="handleCardLeave">
            <div class="feature-icon">🛡️</div>
            <h3>Ultra Secure</h3>
            <p>최신 보안 기술로 데이터를 안전하게 보호</p>
          </div>

          <div class="feature-card" @mouseenter="handleCardHover" @mouseleave="handleCardLeave">
            <div class="feature-icon">✨</div>
            <h3>AI Powered</h3>
            <p>인공지능 기반 스마트 컨텐츠 관리</p>
          </div>
        </div>

        <!-- CTA 버튼들 -->
        <div class="cta-section">
          <button class="cta-primary" @click="goToPostList">
            <span class="cta-text">지금 시작하기</span>
            <span class="cta-arrow">→</span>
          </button>
        </div>

        <!-- 통계 정보 -->
        <div class="stats-section">
          <div class="stat-item">
            <div class="stat-number">1,247</div>
            <div class="stat-label">활성 사용자</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">8,591</div>
            <div class="stat-label">게시글</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">99.9%</div>
            <div class="stat-label">가동률</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {useRouter} from "vue-router"

const router = useRouter()

const goToPostList = () => {
  router.push('/posts')
}

// 카드 호버 효과
const handleCardHover = (event: Event) => {
  const card = event.currentTarget as HTMLElement
  card.style.transform = 'translateY(-10px) scale(1.05)'
}

const handleCardLeave = (event: Event) => {
  const card = event.currentTarget as HTMLElement
  card.style.transform = 'translateY(0) scale(1)'
}
</script>

<style scoped>
@import "@/assets/styles/components/home.css";
</style>
