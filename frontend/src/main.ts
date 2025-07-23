import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'

// Element Plus 스타일 import
import 'element-plus/dist/index.css'

const app = createApp(App)

// Pinia (상태관리) 등록
app.use(createPinia())

// Vue Router 등록
app.use(router)

// #app 요소에 마운트
app.mount('#app')
