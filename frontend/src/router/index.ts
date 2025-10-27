import {createRouter, createWebHistory} from 'vue-router'
import {useAuthStore} from "@/stores/auth.ts";

const router = createRouter({
    // HTML5 History API 사용 (hash 모드 X)
    history: createWebHistory(),
    routes: [
        {
            path: '/',
            name: 'Home',
            component: () => import('@/views/Home.vue'),
        },
        {
            path: '/login',
            name: 'Login',
            component: () => import('@/views/Login.vue'),
        },
        {
            path: '/signup',
            name: 'Signup',
            component: () => import('@/views/Signup.vue'),
            meta: {requiresAuth: false}
        },
        {
            path: '/mypage',
            name: 'MyPage',
            component: () => import('@/views/MyPage.vue'),
            meta: {requiresAuth: true}
        },
        {
            path: '/posts',
            name: 'PostList',
            component: () => import('@/views/PostList.vue'),
        },
        {
            path: '/posts/create',
            name: 'PostCreate',
            component: () => import('@/views/PostCreate.vue'),
            // 로그인이 필요한 페이지
            meta: {requiresAuth: true}
        },
        {
            path: '/posts/:id/edit',
            name: 'PostEdit',
            component: () => import('@/views/PostEdit.vue'),
            props: true,
            meta: {requiresAuth: true}

        },
        {
            path: '/posts/:id',
            name: 'PostDetail',
            component: () => import('@/views/PostDetail.vue'),
            props: true
        },
        {
            path:'/:pathMatch.(.*)*',
            name: 'NotFound',
            redirect:'/posts'
        }
    ]
})

// 전역 가드: 인증이 필요한 페이지 체크
router.beforeEach((to, _from, next) => {
    const authStore = useAuthStore()

    if (to.meta.requiresAuth && !authStore.isLoggedIn) {
        next('/login')
    } else if ((to.path === '/login' || to.path === '/signup') && authStore.isLoggedIn) {
        next('/posts')
    } else {
        next()
    }
})

export default router
