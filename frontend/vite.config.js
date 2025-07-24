import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

// Element Plus 자동 import를 위한 플러그인들
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [
        vue(), // .vue 파일 처리
        // Vue Composition API 자동 import
        AutoImport({
            resolvers: [ElementPlusResolver()],
            imports: [
                'vue',
                'vue-router',
                'pinia'
            ],
            dts: true, // TypeScript 선언 파일 자동 생성
        }),
        // Element Plus 컴포넌트 자동 import
        Components({
            resolvers: [ElementPlusResolver()],
            dts: true,
        }),
    ],
    resolve: {
        alias: {
            '@': resolve(__dirname, 'src'), // @/ 경로로 src 폴더 접근
        },
    },
    server: {
        port: 8080, // 개발 서버 포트 (3000 → 8080)
        proxy: {
            // 백엔드 API 프록시 설정
            '/api': {
                target: 'http://localhost:9082', // content-service (8082 → 9082)
                changeOrigin: true,
            },
            '/auth': {
                target: 'http://localhost:9081', // auth-service (8081 → 9081)
                changeOrigin: true,
            },
        },
    },
})
