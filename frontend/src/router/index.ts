import {createRouter, createWebHistory} from 'vue-router'
import PostEdit from "@/views/PostEdit.vue";

// 페이지 컴포넌트 lazy loading (성능 최적화)
const Home = () => import('@/views/Home.vue')
const PostList = () => import('@/views/PostList.vue')
const PostDetail = () => import('@/views/PostDetail.vue')
const PostCreate = () => import('@/views/PostCreate.vue')
const Login = () => import('@/views/Login.vue')
const MyPage = () => import('@/views/MyPage.vue')

const router = createRouter({
    // HTML5 History API 사용 (hash 모드 X)
    history: createWebHistory(),
    routes: [
        {
            path: '/',
            name: 'Home',
            component: Home
        },
        {
            path: '/login',
            name: 'Login',
            component: Login
        },
        {
            path: '/mypage',
            name: 'MyPage',
            component: MyPage,
            meta: {requiresAuth: true}
        },
        {
            path: '/posts',
            name: 'PostList',
            component: PostList
        },
        {
            path: '/posts/create',
            name: 'PostCreate',
            component: PostCreate,
            // 로그인이 필요한 페이지
            meta: {requiresAuth: true}
        },
        {
            path: '/posts/:id/edit',
            name: 'PostEdit',
            component: PostEdit,
            props: true,
            meta: {requiresAuth: true}

        },
        {
            path: '/posts/:id',
            name: 'PostDetail',
            component: PostDetail,
            props: true
        }
    ]
})

// 전역 가드: 인증이 필요한 페이지 체크
router.beforeEach((to, _from, next) => {
    const isAuthenticated = localStorage.getItem('token') // JWT 토큰 확인

    if (to.meta.requiresAuth && !isAuthenticated) {
        // 로그인이 필요한 페이지인데 토큰이 없으면 로그인 페이지로
        next('/login')
    } else {
        next()
    }
})

export default router
