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
            <span class="title-line">Modern</span>
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

          <button v-if="!authStore.isLoggedIn" class="cta-secondary" @click="goToLogin">
            <span class="cta-icon">👋</span>
            <span>로그인</span>
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
import { useRouter } from "vue-router"
import { useAuthStore } from "@/stores/auth"

const router = useRouter()
const authStore = useAuthStore()

const goToPostList = () => {
  router.push('/posts')
}

const goToLogin = () => {
  router.push('/login')
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
.home {
  position: relative;
  min-height: calc(100vh - 60px);
  background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 배경 애니메이션 요소들 */
.background-elements {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
}

.floating-shape {
  position: absolute;
  border-radius: 50%;
  background: linear-gradient(45deg, rgba(255,255,255,0.1), rgba(255,255,255,0.05));
  backdrop-filter: blur(10px);
  animation: float 6s ease-in-out infinite;
}

.shape-1 {
  width: 200px;
  height: 200px;
  top: 10%;
  left: 10%;
  animation-delay: 0s;
}

.shape-2 {
  width: 150px;
  height: 150px;
  top: 60%;
  right: 15%;
  animation-delay: 2s;
}

.shape-3 {
  width: 100px;
  height: 100px;
  bottom: 20%;
  left: 20%;
  animation-delay: 4s;
}

.shape-4 {
  width: 80px;
  height: 80px;
  top: 30%;
  right: 30%;
  animation-delay: 1s;
}

@keyframes float {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(180deg); }
}

/* 히어로 섹션 */
.hero-section {
  position: relative;
  z-index: 2;
  width: 100%;
  max-width: 1200px;
  padding: 40px 20px;
}

.hero-content {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  border-radius: 30px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  padding: 60px 40px;
  text-align: center;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.15);
}

/* 타이틀 섹션 */
.title-section {
  margin-bottom: 50px;
}

.main-title {
  font-size: 4rem;
  font-weight: 800;
  line-height: 1.1;
  margin-bottom: 20px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
}

.title-line {
  display: block;
  color: white;
}

.gradient-text {
  background: linear-gradient(45deg, #ff6b6b, #4ecdc4, #45b7d1, #96ceb4);
  background-size: 400% 400%;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  animation: gradientShift 3s ease-in-out infinite;
}

@keyframes gradientShift {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

.subtitle {
  font-size: 1.3rem;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 300;
  max-width: 600px;
  margin: 0 auto;
  line-height: 1.6;
}

/* 기술 배지들 */
.tech-badges {
  display: flex;
  justify-content: center;
  gap: 15px;
  flex-wrap: wrap;
  margin-bottom: 50px;
}

.badge {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border-radius: 25px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: white;
  font-weight: 500;
  transition: all 0.3s ease;
  cursor: pointer;
}

.badge:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
}

.badge-icon {
  font-size: 1.2rem;
}

/* 기능 카드들 */
.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 25px;
  margin-bottom: 50px;
}

.feature-card {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(15px);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  padding: 30px 25px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
}

.feature-icon {
  font-size: 3rem;
  margin-bottom: 15px;
}

.feature-card h3 {
  font-size: 1.4rem;
  font-weight: 600;
  color: white;
  margin-bottom: 10px;
}

.feature-card p {
  color: rgba(255, 255, 255, 0.8);
  line-height: 1.5;
  font-size: 0.95rem;
}

/* CTA 버튼들 */
.cta-section {
  display: flex;
  justify-content: center;
  gap: 20px;
  flex-wrap: wrap;
  margin-bottom: 50px;
}

.cta-primary {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 18px 35px;
  background: linear-gradient(45deg, #ff6b6b, #4ecdc4);
  color: white;
  border: none;
  border-radius: 50px;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}

.cta-primary:hover {
  transform: translateY(-3px);
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.3);
}

.cta-arrow {
  transition: transform 0.3s ease;
}

.cta-primary:hover .cta-arrow {
  transform: translateX(5px);
}

.cta-secondary {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 18px 30px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 50px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.cta-secondary:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-3px);
}

/* 통계 섹션 */
.stats-section {
  display: flex;
  justify-content: center;
  gap: 50px;
  flex-wrap: wrap;
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 2.5rem;
  font-weight: 700;
  color: white;
  margin-bottom: 5px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
}

.stat-label {
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.7);
  text-transform: uppercase;
  letter-spacing: 1px;
}

/* 반응형 디자인 */
@media (max-width: 768px) {
  .hero-content {
    padding: 40px 20px;
  }

  .main-title {
    font-size: 2.8rem;
  }

  .subtitle {
    font-size: 1.1rem;
  }

  .tech-badges {
    gap: 10px;
  }

  .badge {
    padding: 10px 16px;
    font-size: 0.9rem;
  }

  .features-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .cta-section {
    flex-direction: column;
    align-items: center;
  }

  .cta-primary,
  .cta-secondary {
    width: 100%;
    max-width: 300px;
    justify-content: center;
  }

  .stats-section {
    gap: 30px;
  }

  .stat-number {
    font-size: 2rem;
  }
}

@media (max-width: 480px) {
  .main-title {
    font-size: 2.2rem;
  }

  .tech-badges {
    justify-content: center;
  }

  .stats-section {
    gap: 20px;
  }
}
</style>
