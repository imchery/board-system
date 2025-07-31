import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'

// ===== 스타일 임포트 (순서 중요!) =====
// 1. CSS 변수 (최우선)
import './assets/styles/variables.css'

// 2. Element Plus 기본 스타일
import 'element-plus/dist/index.css'

// 3. 공통 스타일
import './assets/styles/common.css'

// 4. Element Plus 커스터마이징 (Element Plus 스타일 다음에 와야 함)
import './assets/styles/element-overrides.css'

// ===== Vue 앱 생성 및 설정 =====
const app = createApp(App)

// Pinia (상태관리) 등록
app.use(createPinia())

// Vue Router 등록
app.use(router)

// #app 요소에 마운트
app.mount('#app')
